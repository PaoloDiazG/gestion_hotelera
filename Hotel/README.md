# Hotel Management System

A comprehensive Java application for managing hotel operations, including room management, guest management, reservations, check-in/check-out, and billing.

## Features

### Dashboard
- Overview of the hotel management system
- Quick access to all main features

### Room Management
- View all rooms with their details (number, floor, type, status, price)
- Add, edit, and delete rooms
- Filter rooms by status and type
- Change room status (Available, Occupied, Maintenance, Cleaning)

### Guest Management
- View all guests with their details (name, ID, contact information)
- Add, edit, and delete guests
- Search guests by name or ID number

### Reservation Management
- View all reservations with their details (guest, room, dates, status)
- Create, edit, and cancel reservations
- Filter reservations by status and guest
- Check room availability for specific dates

### Check-in/Check-out
- Check-in guests with confirmed reservations
- Check-out guests with active stays
- Automatically update room and reservation status

### Billing
- Generate bills for checked-out reservations
- Add additional items to bills
- View bill details with itemized charges
- Mark bills as paid

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Any Java IDE (IntelliJ IDEA, Eclipse, etc.)

### Running the Application
1. Clone the repository
2. Open the project in your IDE
3. Run the `Main` class

## Usage Guide

### Room Management
1. Navigate to the "Rooms" tab
2. Use the "Add Room" button to create a new room
3. Select a room and use "Edit Room" to modify its details
4. Select a room and use "Delete Room" to remove it
5. Select a room and use "Change Status" to update its status
6. Use the filters to view rooms by status or type

### Guest Management
1. Navigate to the "Guests" tab
2. Use the "Add Guest" button to register a new guest
3. Select a guest and use "Edit Guest" to update their information
4. Select a guest and use "Delete Guest" to remove them
5. Use the search field to find guests by name or ID

### Reservation Management
1. Navigate to the "Reservations" tab
2. Use the "Create Reservation" button to make a new reservation
   - Select a guest or create a new one
   - Select a room type
   - Enter check-in and check-out dates
   - Check availability and select a room
3. Select a reservation and use "Edit Reservation" to modify dates
4. Select a reservation and use "Cancel Reservation" to cancel it
5. Use the filters to view reservations by status or guest

### Check-in/Check-out
1. Navigate to the "Check-in/Check-out" tab
2. In the "Check-in" tab, select a reservation and click "Check-in"
3. In the "Check-out" tab, select a reservation and click "Check-out"
4. After check-out, you can choose to generate a bill

### Billing
1. Navigate to the "Billing" tab
2. Use the "Create Bill" button to generate a bill for a checked-out reservation
3. Add additional items if needed
4. View bill details by selecting a bill and clicking "View Bill"
5. Mark bills as paid by selecting them and clicking "Mark as Paid"
6. Use the filter to view all bills, paid bills, or unpaid bills

## System Architecture

The application follows a layered architecture:

1. **Model Layer**: Contains the data models (Room, Guest, Reservation, Bill)
2. **Service Layer**: Contains the business logic (RoomService, GuestService, ReservationService, BillingService)
3. **UI Layer**: Contains the user interface components (MainFrame, RoomsPanel, GuestsPanel, etc.)

## Data Persistence

The current implementation uses in-memory storage for simplicity. In a production environment, this would be replaced with a database.

## Testing

The application includes a comprehensive test suite to ensure functionality, performance, and security. For detailed information about the testing infrastructure, methodologies, and best practices, see the [TESTING.md](TESTING.md) file.

### Test Types

#### Unit Tests
- **Model Tests**: Verify the behavior of data models (Room, Guest, Reservation, Bill)
  - Test constructors, getters/setters, and business methods
  - Ensure data integrity and validation
  - Verify correct behavior of model relationships
- **Service Tests**: Test the business logic in service classes (RoomService, GuestService, ReservationService, BillingService)
  - Test CRUD operations (Create, Read, Update, Delete)
  - Verify business rules and constraints
  - Test edge cases and error handling

#### Integration Tests
- Test the interaction between different components of the system
  - Verify that services work together correctly
  - Test complete workflows from start to finish (e.g., from creating a guest to checking out and billing)
  - Ensure data consistency across services

#### Functional Tests
- Test the UI components and their behavior
  - Verify that UI components are created correctly
  - Test internationalization (i18n) and localization
  - Ensure UI components respond correctly to user interactions
  - Test color schemes and visual elements

#### Performance Tests
- Measure the time it takes to perform various operations
  - Test system performance with large amounts of data
  - Identify potential performance bottlenecks
  - Ensure the system meets performance requirements

#### Security Tests
- Test input validation and data sanitization
  - Verify that the system handles malicious inputs correctly
  - Test access control mechanisms
  - Ensure concurrent operations don't lead to security issues

### Testing Framework

The project uses JUnit 4 as the primary testing framework, along with:
- Java Swing Testing for UI component testing
- System.currentTimeMillis() for performance measurements
- Multithreading for concurrency testing

### Running the Tests
1. Open the project in your IDE
2. Navigate to the `src/test` directory
3. Run the tests using your IDE's test runner or JUnit

### Test Coverage

The test suite aims to cover all critical functionality of the Hotel Management System:
- **Model Classes**: 100% coverage of all model classes
- **Service Classes**: 100% coverage of all service methods
- **UI Components**: Coverage of all critical UI components
- **Workflows**: Coverage of all critical workflows

### Potential Improvements

The testing infrastructure could be improved in several ways:
- **Mockito**: For more isolated unit testing
- **JaCoCo**: For code coverage reporting
- **Selenium**: For more comprehensive UI testing
- **JMeter**: For more sophisticated performance testing
- **OWASP ZAP**: For more comprehensive security testing

For more detailed information about these improvements and how to implement them, see the [TESTING.md](TESTING.md) file.

## Internationalization

The application supports multiple languages:
- Currently implemented: Spanish (es_ES)
- All UI text is externalized in properties files
- The `Messages` class handles loading and retrieving translations

## Future Enhancements

- Database integration for persistent data storage
- User authentication and role-based access control
- Reporting and analytics
- Email notifications for reservations and bills
- Online booking integration
- Additional language support
