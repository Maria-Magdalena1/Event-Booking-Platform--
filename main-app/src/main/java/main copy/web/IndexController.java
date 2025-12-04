package main.web;

import jakarta.validation.Valid;
import main.entities.User;
import main.exceptions.EmailRegisteredException;
import main.exceptions.UsernameTakenException;
import main.microservices.AnalyticsClient;
import main.security.UserData;
import main.services.EventService;
import main.services.UserService;
import main.web.dto.EventDTO;
import main.web.dto.UserLoginDTO;
import main.web.dto.UserRegistrationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class IndexController {

    private final UserService userService;
    private final EventService eventService;
    private final AnalyticsClient analyticsClient;

    @Autowired
    public IndexController(UserService userService, EventService eventService, AnalyticsClient analyticsClient) {
        this.userService = userService;
        this.eventService = eventService;
        this.analyticsClient = analyticsClient;
    }

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/login")
    public ModelAndView loginPage() {
        ModelAndView mav = new ModelAndView("login");
        mav.addObject("loginDTO", new UserLoginDTO());
        return mav;
    }

    @GetMapping("/register")
    public ModelAndView showRegistrationForm() {
        ModelAndView mav = new ModelAndView("register");
        mav.addObject("registerRequest", new UserRegistrationDTO());
        return mav;
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid @ModelAttribute("registerRequest") UserRegistrationDTO userDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        ModelAndView mav = new ModelAndView("register");

        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.userDTO", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return mav;
        }

        try {
            User user = userService.register(userDTO);
            analyticsClient.addUser(userService.mapToAnalyticDTO(user));
        } catch (UsernameTakenException e) {
            bindingResult.rejectValue("username", "error.userDTO", "Username taken");
            return mav;
        } catch (EmailRegisteredException e) {
            bindingResult.rejectValue("email", "error.userDTO", "Email already taken");
            return mav;
        }
        redirectAttributes.addFlashAttribute("registrationSuccessful", "Registration Successful");
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView homePage(@AuthenticationPrincipal UserData userData) {

        User user = userService.findById(userData.getUserId());

        ModelAndView modelAndView = new ModelAndView("home");
        List<EventDTO> events = eventService.findUpcomingEvents()
                .stream()
                .map(event -> {
                    EventDTO dto = eventService.mapToDTO(event);

                    UUID creatorId = dto.getCreator() != null ? dto.getCreator().getId() : null;

                    boolean canBook = dto.getAvailableSeats() != null
                            && dto.getAvailableSeats() > 0
                            && (creatorId == null || !creatorId.equals(user.getId()));
                    dto.setCanBook(canBook);

                    return dto;
                })
                .limit(5)
                .collect(Collectors.toList());
        modelAndView.addObject("user", user);
        modelAndView.addObject("events", events);
        return modelAndView;
    }
}
