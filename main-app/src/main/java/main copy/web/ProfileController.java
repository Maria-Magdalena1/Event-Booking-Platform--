package main.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import main.entities.Booking;
import main.entities.Event;
import main.entities.Role;
import main.entities.User;
import main.security.UserData;
import main.services.BookingService;
import main.services.EventService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import main.services.UserService;
import main.web.dto.UserDTO;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@AllArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final EventService eventService;
    private final BookingService bookingService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView viewProfile(@AuthenticationPrincipal UserData userData) {
        User user = userService.findById(userData.getUserId());

        List<Event> createdEvents = eventService.findEventsByCreator(user);
        List<Booking> bookings = bookingService.findByUser(user);
        ModelAndView mav = new ModelAndView("users/profile");
        mav.addObject("user", user);
        mav.addObject("createdEvents", createdEvents);
        mav.addObject("bookings", bookings);
        mav.addObject("isAdmin", user.getRole() == Role.ADMIN);

        if (user.getRole() == Role.ADMIN) {
            List<User> allUsers = userService.findAllUsers()
                    .stream()
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toList());

            mav.addObject("allUsers", allUsers);
        }
        return mav;
    }

    @GetMapping("profile/edit")
    public ModelAndView showEditProfile(@AuthenticationPrincipal UserData userData) {
        UserDTO user = userService.mapToDTO(userService.findById(userData.getUserId()));
        if (user.getName() == null) user.setName("");
        if (user.getAge() == null) user.setAge(0);
        return new ModelAndView("users/edit-profile", "user", user);
    }

    @PutMapping("profile/edit")
    public ModelAndView editProfile(@ModelAttribute("user") @Valid UserDTO userDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("users/edit-profile");
            mav.addObject("user", userDTO);
            return mav;
        }

        userService.update(userDTO.getId(), userDTO);
        redirectAttributes.addFlashAttribute("message", "User updated successfully");
        return new ModelAndView("redirect:/user/profile");
    }

    @PostMapping("/block/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleBlockUser(@PathVariable UUID userId) {
        userService.toggleBlockUser(userId);
        return "redirect:/user/profile";
    }
}
