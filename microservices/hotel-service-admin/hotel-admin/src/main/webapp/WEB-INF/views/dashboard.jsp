<%--
&lt;%&ndash;
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hotel Management Dashboard</title>
    <jsp:include page="../../resources/gobhutanCss.jsp"/>
    <link rel="stylesheet" href="<c:url value="../../resources/goBhutanCss/custom.css"/>"/>
</head>
<body>
<!-- Navigation Bar -->
<nav class="navbar navbar-expand-lg navbar-light bg-white sticky-top">
    <div class="container-fluid">
        <!-- Brand -->
        <a class="navbar-brand d-flex align-items-center" href="#">
            <i class="bi bi-building me-2 text-primary fs-4"></i>
            HotelManager Drakpa
        </a>

        <!-- Mobile Toggle Button -->
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>

        <!-- Navigation Items -->
        <div class="collapse navbar-collapse" id="navbarNav">
            <!-- Left Menu Items -->
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link active" href="#"><i class="bi bi-speedometer2 me-1"></i> Dashboard</a>
                </li>

                <!-- Reservations Dropdown -->
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                        <i class="bi bi-calendar-check me-1"></i> Reservations
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#"><i class="bi bi-plus-circle me-2"></i>New Booking</a></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-list-check me-2"></i>All Reservations</a></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-clock me-2"></i>Pending Confirmations</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li class="dropdown-submenu">
                            <a class="dropdown-item dropdown-toggle" href="#" data-bs-toggle="dropdown">
                                <i class="bi bi-funnel me-2"></i>Filter by Status
                            </a>
                            <ul class="dropdown-menu dropdown-submenu-left">
                                <li><a class="dropdown-item" href="#">Confirmed</a></li>
                                <li><a class="dropdown-item" href="#">Cancelled</a></li>
                                <li><a class="dropdown-item" href="#">No-show</a></li>
                            </ul>
                        </li>
                    </ul>
                </li>

                <!-- Rooms Dropdown -->
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                        <i class="bi bi-door-open me-1"></i> Rooms
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#"><i class="bi bi-grid me-2"></i>Room Overview</a></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-house me-2"></i>Available Rooms</a></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-house-fill me-2"></i>Occupied Rooms</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li class="dropdown-submenu">
                            <a class="dropdown-item dropdown-toggle" href="#" data-bs-toggle="dropdown">
                                <i class="bi bi-gear me-2"></i>Room Management
                            </a>
                            <ul class="dropdown-menu dropdown-submenu-left">
                                <li><a class="dropdown-item" href="#">Maintenance</a></li>
                                <li><a class="dropdown-item" href="#">Housekeeping</a></li>
                                <li><a class="dropdown-item" href="#">Room Types</a></li>
                            </ul>
                        </li>
                    </ul>
                </li>

                <!-- Guests Dropdown -->
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                        <i class="bi bi-people me-1"></i> Guests
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#"><i class="bi bi-person-plus me-2"></i>Add Guest</a></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-people-fill me-2"></i>Guest Directory</a></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-box-arrow-in-right me-2"></i>Check-in</a></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-box-arrow-right me-2"></i>Check-out</a></li>
                    </ul>
                </li>

                <!-- Reports Dropdown -->
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                        <i class="bi bi-bar-chart me-1"></i> Reports
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#"><i class="bi bi-graph-up me-2"></i>Revenue Report</a></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-calendar3 me-2"></i>Occupancy Report</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li class="dropdown-submenu">
                            <a class="dropdown-item dropdown-toggle" href="#" data-bs-toggle="dropdown">
                                <i class="bi bi-file-earmark-text me-2"></i>Custom Reports
                            </a>
                            <ul class="dropdown-menu dropdown-submenu-left">
                                <li><a class="dropdown-item" href="#">Daily Report</a></li>
                                <li><a class="dropdown-item" href="#">Monthly Summary</a></li>
                                <li><a class="dropdown-item" href="#">Guest Analytics</a></li>
                            </ul>
                        </li>
                    </ul>
                </li>
            </ul>

            <!-- Search Bar -->
            <div class="search-container me-3">
                <div class="input-group">
                        <span class="input-group-text bg-light border-end-0">
                            <i class="bi bi-search text-muted"></i>
                        </span>
                    <input type="text" class="form-control border-start-0 ps-0" placeholder="Search guests, rooms, bookings...">
                </div>
            </div>

            <!-- User Authentication -->
            <div class="d-flex align-items-center">
                <!-- Notifications -->
                <div class="dropdown me-2">
                    <button class="btn btn-light position-relative" type="button" data-bs-toggle="dropdown">
                        <i class="bi bi-bell"></i>
                        <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                                3
                            </span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><h6 class="dropdown-header">Notifications</h6></li>
                        <li><a class="dropdown-item" href="#"><small class="text-muted">5 min ago</small><br>New booking received</a></li>
                        <li><a class="dropdown-item" href="#"><small class="text-muted">1 hour ago</small><br>Room 101 ready for check-in</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-center" href="#">View all notifications</a></li>
                    </ul>
                </div>

                <!-- User Menu -->
                <div class="dropdown">
                    <button class="btn btn-light dropdown-toggle d-flex align-items-center" type="button" data-bs-toggle="dropdown">
                        <img src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face" alt="User" class="user-avatar me-2">
                        <span class="d-none d-md-inline">Padra</span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item" href="#"><i class="bi bi-person me-2"></i>Profile</a></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-gear me-2"></i>Settings</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-box-arrow-right me-2"></i>Sign Out</a></li>
                    </ul>
                </div>

                <!-- Sign In/Sign Up Buttons (for non-authenticated users) -->
                <div class="d-none" id="authButtons">
                    <a href="loginSignUp/login.jsp" class="btn btn-outline-primary btn-auth">Sign In</a>
                    <a href="register.jsp" class="btn btn-primary btn-auth">Sign Up</a>
                </div>
            </div>
        </div>
    </div>
</nav>

<!-- Main Content -->
<main class="main-content">
    <div class="container-fluid">
        <!-- Welcome Section -->
        <div class="welcome-section">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h2 class="mb-2">Welcome back, Padra! 👋</h2>
                    <p class="mb-0">Here's what's happening at your hotel today</p>
                </div>
                <div class="col-md-4 text-md-end">
                    <div class="d-flex align-items-center justify-content-md-end">
                        <i class="bi bi-calendar3 me-2 fs-5"></i>
                        <span id="currentDate"></span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Statistics Cards -->
        <div class="row g-4 mb-4">
            <div class="col-lg-3 col-md-6">
                <div class="card stats-card booked h-100">
                    <div class="card-body text-center">
                        <i class="bi bi-check-circle-fill fs-1 mb-3"></i>
                        <h2 class="stats-number">142</h2>
                        <p class="stats-label">Hotels Booked</p>
                        <small class="opacity-75">
                            <i class="bi bi-arrow-up me-1"></i>
                            +12% from last month
                        </small>
                    </div>
                </div>
            </div>

            <div class="col-lg-3 col-md-6">
                <div class="card stats-card pending h-100">
                    <div class="card-body text-center">
                        <i class="bi bi-clock-fill fs-1 mb-3"></i>
                        <h2 class="stats-number">28</h2>
                        <p class="stats-label">Pending Confirmation</p>
                        <small class="opacity-75">
                            <i class="bi bi-arrow-down me-1"></i>
                            -5% from yesterday
                        </small>
                    </div>
                </div>
            </div>

            <div class="col-lg-3 col-md-6">
                <div class="card stats-card checkin h-100">
                    <div class="card-body text-center">
                        <i class="bi bi-door-open-fill fs-1 mb-3"></i>
                        <h2 class="stats-number">86</h2>
                        <p class="stats-label">Check-ins Today</p>
                        <small class="opacity-75">
                            <i class="bi bi-arrow-up me-1"></i>
                            +8% from last week
                        </small>
                    </div>
                </div>
            </div>

            <div class="col-lg-3 col-md-6">
                <div class="card stats-card h-100" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
                    <div class="card-body text-center">
                        <i class="bi bi-star-fill fs-1 mb-3"></i>
                        <h2 class="stats-number">4.8</h2>
                        <p class="stats-label">Average Rating</p>
                        <small class="opacity-75">
                            <i class="bi bi-arrow-up me-1"></i>
                            +0.2 from last month
                        </small>
                    </div>
                </div>
            </div>
        </div>

        <!-- Quick Actions -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header bg-white">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-lightning-fill text-warning me-2"></i>
                            Quick Actions
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="row g-3">
                            <div class="col-md-2 col-sm-4 col-6">
                                <button class="btn btn-outline-primary w-100 h-100 d-flex flex-column align-items-center justify-content-center p-3">
                                    <i class="bi bi-plus-circle fs-4 mb-2"></i>
                                    <span class="small">New Booking</span>
                                </button>
                            </div>
                            <div class="col-md-2 col-sm-4 col-6">
                                <button class="btn btn-outline-success w-100 h-100 d-flex flex-column align-items-center justify-content-center p-3">
                                    <i class="bi bi-box-arrow-in-right fs-4 mb-2"></i>
                                    <span class="small">Check-in</span>
                                </button>
                            </div>
                            <div class="col-md-2 col-sm-4 col-6">
                                <button class="btn btn-outline-info w-100 h-100 d-flex flex-column align-items-center justify-content-center p-3">
                                    <i class="bi bi-box-arrow-right fs-4 mb-2"></i>
                                    <span class="small">Check-out</span>
                                </button>
                            </div>
                            <div class="col-md-2 col-sm-4 col-6">
                                <button class="btn btn-outline-warning w-100 h-100 d-flex flex-column align-items-center justify-content-center p-3">
                                    <i class="bi bi-house fs-4 mb-2"></i>
                                    <span class="small">Room Status</span>
                                </button>
                            </div>
                            <div class="col-md-2 col-sm-4 col-6">
                                <button class="btn btn-outline-secondary w-100 h-100 d-flex flex-column align-items-center justify-content-center p-3">
                                    <i class="bi bi-graph-up fs-4 mb-2"></i>
                                    <span class="small">Reports</span>
                                </button>
                            </div>
                            <div class="col-md-2 col-sm-4 col-6">
                                <button class="btn btn-outline-dark w-100 h-100 d-flex flex-column align-items-center justify-content-center p-3">
                                    <i class="bi bi-gear fs-4 mb-2"></i>
                                    <span class="small">Settings</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent Activity -->
        <div class="row">
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header bg-white d-flex justify-content-between align-items-center">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-clock-history me-2"></i>
                            Recent Activity
                        </h5>
                        <a href="#" class="btn btn-sm btn-outline-primary">View All</a>
                    </div>
                    <div class="card-body p-0">
                        <div class="list-group list-group-flush">
                            <div class="list-group-item d-flex justify-content-between align-items-center">
                                <div class="d-flex align-items-center">
                                    <div class="bg-success rounded-circle p-2 me-3">
                                        <i class="bi bi-check text-white"></i>
                                    </div>
                                    <div>
                                        <h6 class="mb-1">New booking confirmed</h6>
                                        <small class="text-muted">Room 205 - Sarah Johnson</small>
                                    </div>
                                </div>
                                <small class="text-muted">2 min ago</small>
                            </div>
                            <div class="list-group-item d-flex justify-content-between align-items-center">
                                <div class="d-flex align-items-center">
                                    <div class="bg-info rounded-circle p-2 me-3">
                                        <i class="bi bi-door-open text-white"></i>
                                    </div>
                                    <div>
                                        <h6 class="mb-1">Guest checked in</h6>
                                        <small class="text-muted">Room 101 - Mike Wilson</small>
                                    </div>
                                </div>
                                <small class="text-muted">15 min ago</small>
                            </div>
                            <div class="list-group-item d-flex justify-content-between align-items-center">
                                <div class="d-flex align-items-center">
                                    <div class="bg-warning rounded-circle p-2 me-3">
                                        <i class="bi bi-exclamation text-white"></i>
                                    </div>
                                    <div>
                                        <h6 class="mb-1">Maintenance request</h6>
                                        <small class="text-muted">Room 304 - AC issue</small>
                                    </div>
                                </div>
                                <small class="text-muted">1 hour ago</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card">
                    <div class="card-header bg-white">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-calendar-event me-2"></i>
                            Today's Schedule
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="d-flex align-items-center mb-3">
                            <div class="bg-primary rounded-circle p-2 me-3">
                                <i class="bi bi-people text-white small"></i>
                            </div>
                            <div>
                                <h6 class="mb-0">Staff Meeting</h6>
                                <small class="text-muted">9:00 AM</small>
                            </div>
                        </div>
                        <div class="d-flex align-items-center mb-3">
                            <div class="bg-success rounded-circle p-2 me-3">
                                <i class="bi bi-house text-white small"></i>
                            </div>
                            <div>
                                <h6 class="mb-0">Room Inspections</h6>
                                <small class="text-muted">11:00 AM</small>
                            </div>
                        </div>
                        <div class="d-flex align-items-center">
                            <div class="bg-info rounded-circle p-2 me-3">
                                <i class="bi bi-graph-up text-white small"></i>
                            </div>
                            <div>
                                <h6 class="mb-0">Weekly Review</h6>
                                <small class="text-muted">2:00 PM</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>
<jsp:include page="../../resources/gobhutanJs.jsp"/>
<script type="text/javascript" src="<c:url value="../../resources/goBhutanJs/custom.js"/>"></script>
</body>
</html>&ndash;%&gt;

<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hotel Admin Dashboard</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Optional: FontAwesome for icons -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link href="<c:url value='/resources/css/dashboard.css'/>" rel="stylesheet">
</head>
<body>
<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark bg-primary sticky-top">
    <div class="container-fluid">
        <a class="navbar-brand" href="#">HotelAdmin</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarContent">
            <!-- Left Menu -->
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" id="reservationsDropdown" role="button" data-bs-toggle="dropdown">
                        Reservations
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#">New Booking</a></li>
                        <li><a class="dropdown-item" href="#">All Reservations</a></li>
                        <li><a class="dropdown-item" href="#">Pending Confirmations</a></li>
                    </ul>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" id="roomsDropdown" role="button" data-bs-toggle="dropdown">
                        Rooms
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#">Overview</a></li>
                        <li><a class="dropdown-item" href="#">Availability</a></li>
                        <li><a class="dropdown-item" href="#">Management</a></li>
                        <li><a class="dropdown-item" href="#">Maintenance</a></li>
                        <li><a class="dropdown-item" href="#">Housekeeping</a></li>
                    </ul>
                </li>
                <li class="nav-item"><a class="nav-link" href="#">Guests</a></li>
                <li class="nav-item"><a class="nav-link" href="#">Reports</a></li>
            </ul>
            <!-- Right Search & User -->
            <form class="d-flex me-3">
                <input class="form-control me-2" type="search" placeholder="Search..." aria-label="Search">
                <button class="btn btn-outline-light" type="submit"><i class="fa fa-search"></i></button>
            </form>
            <ul class="navbar-nav mb-2 mb-lg-0">
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" id="userDropdown" role="button" data-bs-toggle="dropdown">
                        <img src="<c:url value='/resources/images/avatar.png'/>" class="rounded-circle" width="30" alt="User"> Admin
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item" href="#">Profile</a></li>
                        <li><a class="dropdown-item" href="#">Settings</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#">Sign Out</a></li>
                    </ul>
                </li>
                <li class="nav-item position-relative ms-3">
                    <a class="nav-link" href="#">
                        <i class="fa fa-bell"></i>
                        <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                            3
                        </span>
                    </a>
                </li>
            </ul>
        </div>
    </div>
</nav>

<!-- Main Container -->
<div class="container-fluid mt-4">
    <div class="row">
        <!-- Statistics Cards -->
        <div class="col-lg-3 col-md-6 mb-4">
            <div class="card text-white bg-gradient-primary h-100">
                <div class="card-body">
                    <h5 class="card-title">Hotels Booked</h5>
                    <p class="card-text fs-3">142 <span class="text-success"><i class="fa fa-arrow-up"></i> 12%</span></p>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-md-6 mb-4">
            <div class="card text-white bg-gradient-warning h-100">
                <div class="card-body">
                    <h5 class="card-title">Pending Confirmations</h5>
                    <p class="card-text fs-3">28</p>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-md-6 mb-4">
            <div class="card text-white bg-gradient-success h-100">
                <div class="card-body">
                    <h5 class="card-title">Check-ins Today</h5>
                    <p class="card-text fs-3">86</p>
                </div>
            </div>
        </div>
        <div class="col-lg-3 col-md-6 mb-4">
            <div class="card text-white bg-gradient-info h-100">
                <div class="card-body">
                    <h5 class="card-title">Average Rating</h5>
                    <p class="card-text fs-3">4.8</p>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <!-- Quick Actions -->
        <div class="col-lg-3 mb-4">
            <div class="list-group">
                <a href="#" class="list-group-item list-group-item-action">New Booking</a>
                <a href="#" class="list-group-item list-group-item-action">Add Room</a>
                <a href="#" class="list-group-item list-group-item-action">Add Staff</a>
                <a href="#" class="list-group-item list-group-item-action">Generate Report</a>
            </div>
        </div>

        <!-- Recent Activities / Today's Schedule -->
        <div class="col-lg-6 mb-4">
            <div class="card h-100">
                <div class="card-header">
                    Recent Activities
                </div>
                <ul class="list-group list-group-flush">
                    <li class="list-group-item">Booking #1023 confirmed by John</li>
                    <li class="list-group-item">Room 204 marked under maintenance</li>
                    <li class="list-group-item">Payment of $320 received from guest #558</li>
                    <li class="list-group-item">Staff Jane scheduled for housekeeping</li>
                </ul>
            </div>
        </div>

        <div class="col-lg-3 mb-4">
            <div class="card h-100">
                <div class="card-header">
                    Today's Schedule
                </div>
                <ul class="list-group list-group-flush">
                    <li class="list-group-item">Check-in: Room 101</li>
                    <li class="list-group-item">Check-out: Room 205</li>
                    <li class="list-group-item">Maintenance: AC repair Room 203</li>
                    <li class="list-group-item">Staff Meeting: 3 PM</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
--%>


<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hotel Admin Dashboard</title>

    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <!-- Custom Styles -->
    <style>
        :root {
            --primary-gradient: linear-gradient(135deg, #6e45e2, #88d3ce);
            --secondary-gradient: linear-gradient(135deg, #ff6b6b, #feca57);
            --success-gradient: linear-gradient(135deg, #1dd1a1, #5f27cd);
            --info-gradient: linear-gradient(135deg, #54a0ff, #c8d6e5);
            --warning-gradient: linear-gradient(135deg, #ff9ff3, #f368e0);
        }

        body {
            background-color: #f8f9fa;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .navbar-brand img {
            height: 32px;
        }

        .stat-card {
            border-radius: 16px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.08);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            cursor: pointer;
            overflow: hidden;
        }

        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 12px 30px rgba(0,0,0,0.15);
        }

        .stat-card .card-body {
            padding: 1.5rem;
            color: white;
        }

        .stat-icon {
            font-size: 2.5rem;
            opacity: 0.9;
        }

        .growth-indicator {
            font-size: 0.85rem;
            font-weight: 600;
        }

        .dropdown-hover:hover .dropdown-menu {
            display: block;
            margin-top: 0;
        }

        .sidebar {
            height: calc(100vh - 56px);
            overflow-y: auto;
            background: #fff;
            box-shadow: 0 0 15px rgba(0,0,0,0.05);
            border-right: 1px solid #eee;
        }

        .sidebar .nav-link {
            color: #495057;
            padding: 12px 20px;
            border-radius: 8px;
            margin: 4px 12px;
            transition: all 0.2s;
        }

        .sidebar .nav-link:hover,
        .sidebar .nav-link.active {
            background: var(--primary-gradient);
            color: white !important;
        }

        .recent-activity-item {
            border-left: 3px solid #6e45e2;
            padding-left: 15px;
            margin-bottom: 15px;
        }

        .quick-action-btn {
            border-radius: 50px;
            padding: 10px 20px;
            font-weight: 500;
        }

        .notification-badge {
            position: absolute;
            top: -5px;
            right: -5px;
            font-size: 0.7rem;
        }

        @media (max-width: 991.98px) {
            .sidebar {
                height: auto;
                border-right: none;
                border-bottom: 1px solid #eee;
            }
        }

        .animated-pulse {
            animation: pulse 2s infinite;
        }

        @keyframes pulse {
            0% { opacity: 1; }
            50% { opacity: 0.6; }
            100% { opacity: 1; }
        }

        .progress-sm {
            height: 8px;
        }
    </style>
</head>
<body>

<!-- 🔷 Sticky Responsive Navbar -->
<nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm sticky-top">
    <div class="container-fluid">
        <!-- Brand -->
        <a class="navbar-brand d-flex align-items-center" href="#">
            <img src="https://via.placeholder.com/150x40?text=HOTEL+LOGO" alt="Hotel Logo">
        </a>

        <!-- Mobile Toggle -->
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <!-- Navbar Links -->
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <!-- 📋 Multi-level Dropdown Menus -->
                <li class="nav-item dropdown-hover">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-calendar-check"></i> Reservations
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#">New Booking</a></li>
                        <li><a class="dropdown-item" href="#">All Reservations</a></li>
                        <li><a class="dropdown-item" href="#">Pending Confirmations</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#">Status Filters</a></li>
                    </ul>
                </li>

                <li class="nav-item dropdown-hover">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-door-open"></i> Rooms
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#">Overview</a></li>
                        <li><a class="dropdown-item" href="#">Availability</a></li>
                        <li><a class="dropdown-item" href="#">Management</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#">Maintenance</a></li>
                        <li><a class="dropdown-item" href="#">Housekeeping</a></li>
                    </ul>
                </li>

                <li class="nav-item">
                    <a class="nav-link" href="#"><i class="bi bi-people"></i> Guests</a>
                </li>

                <li class="nav-item dropdown-hover">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-bar-chart"></i> Reports
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="#">Revenue</a></li>
                        <li><a class="dropdown-item" href="#">Occupancy</a></li>
                        <li><a class="dropdown-item" href="#">Custom Reports</a></li>
                    </ul>
                </li>
            </ul>

            <!-- 🔍 Search Bar -->
            <form class="d-flex me-3" role="search">
                <div class="input-group" style="max-width: 300px;">
                    <span class="input-group-text bg-white border-end-0"><i class="bi bi-search"></i></span>
                    <input class="form-control border-start-0" type="search" placeholder="Search guest, room, booking..." aria-label="Search">
                </div>
            </form>

            <!-- 🔐 Authentication & Notifications -->
            <div class="d-flex align-items-center">
                <!-- Notification Bell -->
                <div class="position-relative me-3">
                    <a href="#" class="text-decoration-none text-dark position-relative">
                        <i class="bi bi-bell fs-5"></i>
                        <span class="badge bg-danger notification-badge">3</span>
                    </a>
                </div>

                <!-- User Dropdown -->
                <div class="dropdown">
                    <a class="d-flex align-items-center text-decoration-none dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <img src="https://via.placeholder.com/32" alt="Avatar" class="rounded-circle me-2" width="32" height="32">
                        <span>Admin</span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item" href="#"><i class="bi bi-person-circle"></i> Profile</a></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-gear"></i> Settings</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#"><i class="bi bi-box-arrow-right"></i> Sign Out</a></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</nav>

<!-- 📊 Main Dashboard Layout -->
<div class="container-fluid">
    <div class="row">

        <!-- Sidebar Navigation (Collapsible on Mobile) -->
        <div class="col-lg-2 col-md-3 sidebar p-3 mt-3 mt-lg-0">
            <h6 class="text-muted mb-3">ADMIN NAVIGATION</h6>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link active" href="#"><i class="bi bi-speedometer2 me-2"></i> Dashboard</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#"><i class="bi bi-houses me-2"></i> Room Management</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#"><i class="bi bi-tools me-2"></i> Maintenance</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#"><i class="bi bi-cart me-2"></i> Inventory</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#"><i class="bi bi-currency-dollar me-2"></i> Financials</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#"><i class="bi bi-graph-up me-2"></i> Analytics</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#"><i class="bi bi-star me-2"></i> Guest Feedback</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#"><i class="bi bi-gear me-2"></i> System Settings</a>
                </li>
            </ul>

            <!-- ⚡ Quick Actions -->
            <div class="mt-4">
                <h6 class="text-muted mb-2">QUICK ACTIONS</h6>
                <div class="d-grid gap-2">
                    <button class="btn btn-outline-primary quick-action-btn"><i class="bi bi-plus-circle"></i> New Booking</button>
                    <button class="btn btn-outline-success quick-action-btn"><i class="bi bi-check-circle"></i> Check-in Guest</button>
                    <button class="btn btn-outline-warning quick-action-btn"><i class="bi bi-exclamation-triangle"></i> Report Issue</button>
                </div>
            </div>
        </div>

        <!-- Main Content -->
        <div class="col-lg-10 col-md-9 p-4">
            <!-- Header -->
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h3><i class="bi bi-speedometer2 me-2"></i> Admin Dashboard</h3>
                <span class="text-muted">Today: <strong><%= new java.text.SimpleDateFormat("EEEE, MMMM d, yyyy").format(new java.util.Date()) %></strong></span>
            </div>

            <!-- 📊 Statistics Cards -->
            <div class="row g-4 mb-4">
                <!-- Hotels Booked -->
                <div class="col-xl-3 col-md-6">
                    <div class="card stat-card text-white" style="background: var(--primary-gradient);">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h6 class="mb-1">Hotels Booked</h6>
                                    <h2 class="mb-0">142</h2>
                                    <span class="growth-indicator text-success"><i class="bi bi-arrow-up"></i> +12%</span>
                                </div>
                                <i class="bi bi-building stat-icon"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Pending Confirmations -->
                <div class="col-xl-3 col-md-6">
                    <div class="card stat-card text-white" style="background: var(--secondary-gradient);">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h6 class="mb-1">Pending Confirmation</h6>
                                    <h2 class="mb-0">28</h2>
                                    <span class="growth-indicator text-warning"><i class="bi bi-arrow-right"></i> 0%</span>
                                </div>
                                <i class="bi bi-hourglass-split stat-icon"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Check-ins Today -->
                <div class="col-xl-3 col-md-6">
                    <div class="card stat-card text-white" style="background: var(--success-gradient);">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h6 class="mb-1">Check-ins Today</h6>
                                    <h2 class="mb-0">86</h2>
                                    <span class="growth-indicator text-success"><i class="bi bi-arrow-up"></i> +8%</span>
                                </div>
                                <i class="bi bi-door-open stat-icon"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Average Rating -->
                <div class="col-xl-3 col-md-6">
                    <div class="card stat-card text-white" style="background: var(--info-gradient);">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start">
                                <div>
                                    <h6 class="mb-1">Average Rating</h6>
                                    <h2 class="mb-0">4.8</h2>
                                    <div class="text-warning">
                                        <i class="bi bi-star-fill"></i>
                                        <i class="bi bi-star-fill"></i>
                                        <i class="bi bi-star-fill"></i>
                                        <i class="bi bi-star-fill"></i>
                                        <i class="bi bi-star-half"></i>
                                    </div>
                                </div>
                                <i class="bi bi-star stat-icon"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 🚀 Dashboard Layout: Two Columns -->
            <div class="row g-4">
                <!-- Left Column -->
                <div class="col-xl-8">
                    <!-- Today's Schedule -->
                    <div class="card mb-4 shadow-sm">
                        <div class="card-header bg-white d-flex justify-content-between align-items-center">
                            <h5 class="mb-0"><i class="bi bi-calendar-event me-2"></i> Today's Schedule</h5>
                            <button class="btn btn-sm btn-outline-primary">View All</button>
                        </div>
                        <div class="card-body">
                            <div class="list-group list-group-flush">
                                <div class="list-group-item d-flex justify-content-between align-items-center">
                                    <div>
                                        <h6 class="mb-1">Check-in: John Doe (Room 304)</h6>
                                        <small class="text-muted">10:00 AM • Deluxe Suite</small>
                                    </div>
                                    <span class="badge bg-success">Confirmed</span>
                                </div>
                                <div class="list-group-item d-flex justify-content-between align-items-center">
                                    <div>
                                        <h6 class="mb-1">Maintenance: AC Repair (Room 210)</h6>
                                        <small class="text-muted">11:30 AM • Assigned to Tech #3</small>
                                    </div>
                                    <span class="badge bg-warning">Pending</span>
                                </div>
                                <div class="list-group-item d-flex justify-content-between align-items-center">
                                    <div>
                                        <h6 class="mb-1">Check-out: Sarah Lee (Room 501)</h6>
                                        <small class="text-muted">02:00 PM • Ocean View</small>
                                    </div>
                                    <span class="badge bg-info">Scheduled</span>
                                </div>
                                <div class="list-group-item d-flex justify-content-between align-items-center">
                                    <div>
                                        <h6 class="mb-1">Inventory Restock: Housekeeping Supplies</h6>
                                        <small class="text-muted">04:00 PM • Floor 3 Storage</small>
                                    </div>
                                    <span class="badge bg-primary">Planned</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Recent Activity Feed -->
                    <div class="card shadow-sm">
                        <div class="card-header bg-white d-flex justify-content-between align-items-center">
                            <h5 class="mb-0"><i class="bi bi-activity me-2"></i> Recent Activity</h5>
                            <button class="btn btn-sm btn-outline-secondary">Refresh</button>
                        </div>
                        <div class="card-body">
                            <div class="recent-activity-item">
                                <h6 class="mb-1">New booking created by <strong>Receptionist A</strong></h6>
                                <small class="text-muted">2 min ago • Booking #HB-8827</small>
                            </div>
                            <div class="recent-activity-item">
                                <h6 class="mb-1">Room 405 marked as <strong>Cleaned</strong></h6>
                                <small class="text-muted">15 min ago • By Housekeeping Staff</small>
                            </div>
                            <div class="recent-activity-item">
                                <h6 class="mb-1">Payment received: <strong>$285.50</strong> via Credit Card</h6>
                                <small class="text-muted">1 hour ago • Guest: Emily Chen</small>
                            </div>
                            <div class="recent-activity-item">
                                <h6 class="mb-1">Maintenance request <strong>completed</strong> for Room 102</h6>
                                <small class="text-muted">3 hours ago • Plumber assigned</small>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Right Column -->
                <div class="col-xl-4">
                    <!-- Alerts & Notifications -->
                    <div class="card mb-4 shadow-sm">
                        <div class="card-header bg-white">
                            <h5 class="mb-0"><i class="bi bi-exclamation-triangle-fill text-warning me-2"></i> Alerts</h5>
                        </div>
                        <div class="card-body">
                            <div class="alert alert-danger d-flex align-items-center" role="alert">
                                <i class="bi bi-exclamation-circle-fill me-2"></i>
                                <div>
                                    <strong>Low Stock:</strong> Toiletries running low in Floor 3.
                                </div>
                            </div>
                            <div class="alert alert-warning d-flex align-items-center" role="alert">
                                <i class="bi bi-tools me-2"></i>
                                <div>
                                    <strong>Maintenance:</strong> 5 pending work orders (High Priority).
                                </div>
                            </div>
                            <div class="alert alert-info d-flex align-items-center" role="alert">
                                <i class="bi bi-credit-card me-2"></i>
                                <div>
                                    <strong>Payments:</strong> 3 failed transactions need review.
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Performance Metrics -->
                    <div class="card mb-4 shadow-sm">
                        <div class="card-header bg-white">
                            <h5 class="mb-0"><i class="bi bi-graph-up-arrow me-2"></i> Performance</h5>
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <div class="d-flex justify-content-between">
                                    <small>Occupancy Rate</small>
                                    <small>82%</small>
                                </div>
                                <div class="progress progress-sm">
                                    <div class="progress-bar bg-success" role="progressbar" style="width: 82%" aria-valuenow="82" aria-valuemin="0" aria-valuemax="100"></div>
                                </div>
                            </div>
                            <div class="mb-3">
                                <div class="d-flex justify-content-between">
                                    <small>ADR (Avg Daily Rate)</small>
                                    <small>$185</small>
                                </div>
                                <div class="progress progress-sm">
                                    <div class="progress-bar bg-info" role="progressbar" style="width: 75%" aria-valuenow="75" aria-valuemin="0" aria-valuemax="100"></div>
                                </div>
                            </div>
                            <div class="mb-3">
                                <div class="d-flex justify-content-between">
                                    <small>RevPAR</small>
                                    <small>$152</small>
                                </div>
                                <div class="progress progress-sm">
                                    <div class="progress-bar bg-primary" role="progressbar" style="width: 68%" aria-valuenow="68" aria-valuemin="0" aria-valuemax="100"></div>
                                </div>
                            </div>
                            <div class="text-center mt-3">
                                <button class="btn btn-outline-primary btn-sm">View Full Analytics</button>
                            </div>
                        </div>
                    </div>

                    <!-- Quick Stats -->
                    <div class="card shadow-sm">
                        <div class="card-header bg-white d-flex justify-content-between align-items-center">
                            <h5 class="mb-0"><i class="bi bi-lightning me-2"></i> Quick Stats</h5>
                            <div class="animated-pulse text-primary">
                                <i class="bi bi-arrow-clockwise"></i>
                            </div>
                        </div>
                        <div class="card-body">
                            <div class="d-flex justify-content-between mb-2">
                                <span>Rooms Available</span>
                                <strong class="text-success">47</strong>
                            </div>
                            <div class="d-flex justify-content-between mb-2">
                                <span>In Maintenance</span>
                                <strong class="text-warning">5</strong>
                            </div>
                            <div class="d-flex justify-content-between mb-2">
                                <span>Housekeeping Pending</span>
                                <strong class="text-info">12</strong>
                            </div>
                            <div class="d-flex justify-content-between">
                                <span>Refunds Processed</span>
                                <strong class="text-danger">3</strong>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap JS Bundle with Popper -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<!-- Optional: Custom JS for advanced interactions -->
<script>
    // Add hover effect for dropdowns on desktop
    document.addEventListener('DOMContentLoaded', function () {
        if (window.innerWidth > 992) {
            const dropdowns = document.querySelectorAll('.dropdown-hover');
            dropdowns.forEach(dropdown => {
                dropdown.addEventListener('mouseenter', function () {
                    const menu = this.querySelector('.dropdown-menu');
                    menu.classList.add('show');
                });
                dropdown.addEventListener('mouseleave', function () {
                    const menu = this.querySelector('.dropdown-menu');
                    menu.classList.remove('show');
                });
            });
        }
    });
</script>

</body>
</html>