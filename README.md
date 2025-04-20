# Stock Trading Simulation System

A simple stock trading simulation system that allows users to practice trading strategies without financial risk.

## Features

- User authentication (login/register)
- Real-time stock market simulation
- Portfolio management
- Transaction history
- Performance tracking
- Responsive design for mobile and desktop

## Technologies Used

- Java 11
- Spring Boot 2.7.0
- Spring Data JPA
- H2 In-memory Database
- Thymeleaf
- Bootstrap 5
- HTML5/CSS3/JavaScript

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Running the Application

1. Clone the repository

```bash
git clone https://github.com/yourusername/stock-trading-simulation.git
cd stock-trading-simulation
```

2. Build the application

```bash
mvn clean install
```

3. Run the application

```bash
mvn spring-boot:run
```

4. Access the application at http://localhost:8080

### Default Credentials

The application is pre-populated with sample data:

- Username: demo
- Password: password

## Application Structure

- `model` - Contains entity classes
- `repository` - Contains JPA repositories
- `service` - Contains business logic
- `controller` - Contains web controllers
- `templates` - Contains Thymeleaf templates
- `static` - Contains static resources (CSS, JavaScript)

## Potential Enhancements

- Implement price charts and historical data
- Add algorithmic trading capabilities
- Implement a more sophisticated stock price simulation
- Add educational resources and tutorials
- Implement user profile customization
- Add support for multiple portfolios
- Implement social features (leaderboards, following traders)

## License

This project is for educational purposes only. 