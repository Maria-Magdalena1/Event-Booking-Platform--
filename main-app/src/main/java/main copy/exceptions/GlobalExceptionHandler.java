package main.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.access.AccessDeniedException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied() {
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("code", "403");
        mav.addObject("title", "Access Denied");
        mav.addObject("message", "You cannot perform this action.");
        return mav;
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ModelAndView handleEventNotFound(EventNotFoundException ex) {
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("code", "404");
        mav.addObject("title", "Event Not Found");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFound(UserNotFoundException ex) {
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("code", "404");
        mav.addObject("title", "You cannot access this resource.");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("code", "500");
        mav.addObject("title", "Something went wrong");
        mav.addObject("message", "An unexpected error occurred. Please try again later.");
        return mav;
    }
}
