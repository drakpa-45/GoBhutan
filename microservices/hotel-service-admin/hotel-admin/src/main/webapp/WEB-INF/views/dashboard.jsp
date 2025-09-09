<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hotel Management Dashboard</title>
    <jsp:include page="../../resources/gobhutanCss.jsp"/>
    <link rel="stylesheet" href="<c:url value="../../resources/custom.css"/>"/>
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
                    <a href="login.jsp" class="btn btn-outline-primary btn-auth">Sign In</a>
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
<script type="text/javascript" src="<c:url value="../../resources/custom.js"/>"></script>
</body>
</html>