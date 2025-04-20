# Hotel Management System - Testing Documentation

This document provides detailed information about the testing infrastructure, methodologies, and best practices used in the Hotel Management System project.

## Table of Contents
1. [Testing Overview](#testing-overview)
2. [Testing Infrastructure](#testing-infrastructure)
3. [Test Types](#test-types)
   - [Unit Tests](#unit-tests)
   - [Integration Tests](#integration-tests)
   - [Functional Tests](#functional-tests)
   - [Performance Tests](#performance-tests)
   - [Security Tests](#security-tests)
4. [Test Coverage](#test-coverage)
5. [Best Practices](#best-practices)
6. [Continuous Integration](#continuous-integration)
7. [Potential Improvements](#potential-improvements)

## Testing Overview

The Hotel Management System includes a comprehensive test suite to ensure functionality, performance, and security. The tests are organized by type and follow a consistent structure to make them easy to understand and maintain.

## Testing Infrastructure

### Frameworks and Tools

- **JUnit 4**: The primary testing framework used for all test types
- **Java Swing Testing**: For UI component testing
- **System.currentTimeMillis()**: For performance measurements
- **Multithreading**: For concurrency testing

### Directory Structure

```
src/test/
├── model/          # Unit tests for model classes
├── service/        # Unit tests for service classes
├── integration/    # Integration tests
├── functional/     # Functional tests for UI components
├── performance/    # Performance tests
└── security/       # Security tests
```

## Test Types

### Unit Tests

Unit tests verify the behavior of individual components in isolation. In this project, we have unit tests for:

- **Model Classes**: Tests for data models like Room, Guest, Reservation, and Bill
- **Service Classes**: Tests for business logic in service classes like RoomService, GuestService, ReservationService, and BillingService

#### Example: Unit Test for Room Model

```java
@Test
public void testConstructor() {
    // Test the first constructor
    assertEquals(roomNumber, room.getRoomNumber());
    assertEquals(floor, room.getFloor());
    assertEquals(type, room.getType());
    assertEquals(RoomStatus.AVAILABLE, room.getStatus()); // Default status should be AVAILABLE
    assertEquals(pricePerNight, room.getPricePerNight());
    assertEquals("", room.getDescription()); // Default description should be empty string

    // Test the second constructor
    Room room2 = new Room(roomNumber, floor, type, RoomStatus.MAINTENANCE, pricePerNight, description);
    assertEquals(roomNumber, room2.getRoomNumber());
    assertEquals(floor, room2.getFloor());
    assertEquals(type, room2.getType());
    assertEquals(RoomStatus.MAINTENANCE, room2.getStatus());
    assertEquals(pricePerNight, room2.getPricePerNight());
    assertEquals(description, room2.getDescription());
}
```

#### Example: Unit Test for Room Service

```java
@Test
public void testAddRoom() {
    // Add the test room
    Room addedRoom = roomService.addRoom(testRoom);
    
    // Verify the room was added
    assertNotNull(addedRoom);
    assertEquals(testRoom.getRoomNumber(), addedRoom.getRoomNumber());
    
    // Verify we can retrieve the room
    Room retrievedRoom = roomService.getRoomByNumber(testRoom.getRoomNumber());
    assertNotNull(retrievedRoom);
    assertEquals(testRoom.getRoomNumber(), retrievedRoom.getRoomNumber());
    assertEquals(testRoom.getFloor(), retrievedRoom.getFloor());
    assertEquals(testRoom.getType(), retrievedRoom.getType());
    assertEquals(testRoom.getStatus(), retrievedRoom.getStatus());
    assertEquals(testRoom.getPricePerNight(), retrievedRoom.getPricePerNight());
    
    // Clean up
    roomService.deleteRoom(testRoom.getRoomNumber());
}
```

### Integration Tests

Integration tests verify that different components of the system work together correctly. Our integration tests focus on:

- **Service Interactions**: Testing how different services interact with each other
- **Workflow Testing**: Testing complete workflows from start to finish

#### Example: Integration Test for Hotel Workflow

```java
@Test
public void testCompleteHotelWorkflow() {
    // 1. Create a guest
    testGuest = new Guest("Integration", "Test", "INT-TEST-123", "555-INT-TEST", 
                         "integration.test@example.com", "123 Integration St");
    testGuest = guestService.addGuest(testGuest);
    
    assertNotNull("Guest should be created", testGuest);
    assertTrue("Guest should have an ID", testGuest.getId() > 0);
    
    // 2. Create a room
    testRoom = new Room(888, 8, RoomType.DOUBLE, new BigDecimal("200.00"));
    testRoom = roomService.addRoom(testRoom);
    
    // 3. Create a reservation
    testReservation = reservationService.createReservation(testGuest, testRoom, checkInDate, checkOutDate);
    
    // 4. Check in
    boolean checkedIn = reservationService.checkIn(testReservation.getId());
    assertTrue("Check-in should succeed", checkedIn);
    
    // 5. Check out
    boolean checkedOut = reservationService.checkOut(testReservation.getId());
    assertTrue("Check-out should succeed", checkedOut);
    
    // 6. Create a bill
    testBill = billingService.createBill(updatedReservation);
    
    // 7. Mark bill as paid
    boolean marked = billingService.markBillAsPaid(testBill.getId());
    assertTrue("Marking bill as paid should succeed", marked);
}
```

### Functional Tests

Functional tests verify that the UI components work correctly. Our functional tests focus on:

- **UI Component Creation**: Testing that UI components are created correctly
- **Internationalization**: Testing that translations are applied correctly
- **UI Behavior**: Testing that UI components behave as expected

#### Example: Functional Test for UI Components

```java
@Test
public void testUIUtilsComponents() {
    // Test button creation
    JButton primaryButton = UIUtils.createPrimaryButton(Messages.get("button.save"));
    assertEquals("Guardar", primaryButton.getText());
    assertEquals(ColorScheme.PRIMARY, primaryButton.getBackground());
    assertEquals(ColorScheme.TEXT_LIGHT, primaryButton.getForeground());
    assertTrue("Button should be opaque", primaryButton.isOpaque());
    
    // Test label creation
    JLabel titleLabel = UIUtils.createTitleLabel(Messages.get("app.title"));
    assertEquals("Sistema de Gestión Hotelera", titleLabel.getText());
    assertEquals(new Font("Arial", Font.BOLD, 24).getFamily(), titleLabel.getFont().getFamily());
    assertEquals(ColorScheme.TEXT_PRIMARY, titleLabel.getForeground());
}
```

### Performance Tests

Performance tests measure the time it takes to perform various operations. Our performance tests focus on:

- **Operation Timing**: Measuring the time it takes to perform common operations
- **Scalability**: Testing how the system performs with large amounts of data
- **Bottleneck Identification**: Identifying potential performance bottlenecks

#### Example: Performance Test for Guest Creation

```java
@Test
public void testGuestCreationPerformance() {
    System.out.println("Testing guest creation performance...");
    
    long startTime = System.currentTimeMillis();
    
    // Create a large number of guests
    for (int i = 0; i < NUM_GUESTS; i++) {
        Guest guest = new Guest(
            "FirstName" + i,
            "LastName" + i,
            "ID" + i,
            "555-" + i,
            "guest" + i + "@example.com",
            "Address " + i
        );
        guest = guestService.addGuest(guest);
        testGuests.add(guest);
    }
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    System.out.println("Created " + NUM_GUESTS + " guests in " + duration + " ms");
    System.out.println("Average time per guest: " + (duration / (double) NUM_GUESTS) + " ms");
    
    // Verify all guests were created
    assertEquals(NUM_GUESTS, testGuests.size());
}
```

### Security Tests

Security tests verify that the system handles potentially malicious inputs and unauthorized access attempts correctly. Our security tests focus on:

- **Input Validation**: Testing how the system handles potentially malicious inputs
- **Access Control**: Testing that unauthorized access attempts are prevented
- **Concurrency**: Testing that concurrent operations don't lead to security issues

#### Example: Security Test for Input Validation

```java
@Test
public void testInputValidationForGuest() {
    System.out.println("Testing input validation for Guest...");
    
    // Test with very long strings
    String veryLongString = "a".repeat(1000);
    Guest guest = new Guest(veryLongString, veryLongString, veryLongString, veryLongString, veryLongString, veryLongString);
    guest = guestService.addGuest(guest);
    testGuests.add(guest);
    
    // Verify the guest was created
    assertNotNull("Guest should be created even with very long strings", guest);
    
    // Test with special characters
    String specialChars = "!@#$%^&*()_+{}[]|\"':;,.<>?/\\";
    Guest specialGuest = new Guest(specialChars, specialChars, specialChars, specialChars, specialChars, specialChars);
    specialGuest = guestService.addGuest(specialGuest);
    testGuests.add(specialGuest);
    
    // Verify the guest was created
    assertNotNull("Guest should be created even with special characters", specialGuest);
    
    // Test with SQL injection attempt
    String sqlInjection = "' OR 1=1; --";
    Guest sqlGuest = new Guest(sqlInjection, sqlInjection, sqlInjection, sqlInjection, sqlInjection, sqlInjection);
    sqlGuest = guestService.addGuest(sqlGuest);
    testGuests.add(sqlGuest);
    
    // Verify the guest was created
    assertNotNull("Guest should be created even with SQL injection attempt", sqlGuest);
}
```

## Test Coverage

The test suite aims to cover all critical functionality of the Hotel Management System:

- **Model Classes**: 100% coverage of all model classes
- **Service Classes**: 100% coverage of all service methods
- **UI Components**: Coverage of all critical UI components
- **Workflows**: Coverage of all critical workflows

## Best Practices

The test suite follows these best practices:

1. **Isolation**: Each test is isolated from others to prevent interference
2. **Setup and Teardown**: Each test includes proper setup and teardown to ensure a clean environment
3. **Descriptive Names**: Test methods have descriptive names that explain what they're testing
4. **Assertions**: Each test includes clear assertions that verify the expected behavior
5. **Error Messages**: Assertions include descriptive error messages to make failures easier to understand

## Continuous Integration

To implement continuous integration for this project, consider:

1. **GitHub Actions**: Set up GitHub Actions to run tests on every push and pull request
2. **Jenkins**: Set up a Jenkins pipeline to run tests and generate reports
3. **SonarQube**: Integrate SonarQube to analyze code quality and test coverage

Example GitHub Actions workflow:

```yaml
name: Java CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'adopt'
    - name: Build with Maven
      run: mvn -B package --file pom.xml
    - name: Run tests
      run: mvn test
```

## Potential Improvements

The testing infrastructure could be improved in several ways:

1. **Mockito**: Use Mockito to create mock objects for more isolated unit testing
2. **JaCoCo**: Integrate JaCoCo for code coverage reporting
3. **Selenium**: Use Selenium for more comprehensive UI testing
4. **JMeter**: Use JMeter for more sophisticated performance testing
5. **OWASP ZAP**: Use OWASP ZAP for more comprehensive security testing
6. **TestNG**: Consider migrating to TestNG for more advanced testing features
7. **Cucumber**: Implement BDD testing with Cucumber for better collaboration with non-technical stakeholders
8. **Docker**: Use Docker to create isolated test environments
9. **Database Testing**: Add tests for database interactions when database persistence is implemented
10. **API Testing**: Add tests for API endpoints when they are implemented