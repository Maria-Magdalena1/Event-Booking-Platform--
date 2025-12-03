# Event Booking Platform

## Description
**Event Booking Platform** is a web application built using a **microservice architecture**, designed for managing events efficiently. The platform allows users to **browse, book, and manage events**, while providing administrators with analytical insights via a separate data analysis microservice.

**Architecture Overview**
- **Main Application**: Handles user management, event management, and bookings.
- **Data Analysis Service (Microservice)**: Analyzes event data and allows the administrator to perform analysis via a button in the interface.

## Main Application Core Functionalities
- **User Management**:
  * Registration and login of users.
  * Role-based access(admin, user)
- **Event Management**:
  * Admins/organizers can add, edit, and delete events.
  * Events include details such as name, description, dates, price, venue, and location.
- **Booking Management**:
  * Create a booking – users/admins select seats and book an event.
  * Confirm a booking – users/admins confirm a pending booking.
  * Cancel a booking – users/admins cancel a pending booking.
- **Integration**: Provides a REST API for external applications to interact with events and bookings.
- **Data Analysis**: Admins can perform analytics via the Data Analysis Microservice using a dedicated button in the UI.

## Data Analysis Microservice Functionalities
- **Dashboard generation** – aggregated statistics about users, bookings, revenue.
- **Top events and users analysis**| – identifies the most active users and the most booked events.
- **Seat availability warnings** – shows events with critically low available seats.
  
## Technologies
- **Backend**: Java 17, Spring Boot 3.4.0, Spring Data JPA 
- **Database**: MySQL
- **Build Tool**: Maven
- **Frontend**: Spring MVC + Thymeleaf

## Pages and Features
- **Index Page**:
  * Landing page for guests and logged-in users.
  * Guests see login/sign-up buttons and event categories.
  * Logged-in users see personalized options (home, browse events).
- **Events Listing Page**
  * Shows event name, date, location, and category.
  * Buttons for viewing details, editing, or deleting (based on user roles).
- **Add/Edit Event Forms**
  * Form fields: name, description, start/end dates,price, location, venue, total seats...
  * Start date must be before end date.
- **Event Details Page**
  * Displays event details.
  * Button for booking.

## Usage
- Open the app at http://localhost:8080.
- Browse events as a guest or register/log in for full access.
- Create, edit, or delete events if authorized.
- Manage bookings (create, confirm, cancel) via the interface.
- Admins can access analytics via the Data Analysis Microservice.
