<!-- dashboard.jsp -->
<%@ include file="layout/header.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="flex min-h-screen">
    <%@ include file="layout/sidebar.jsp" %>
    
    <!-- Main Content -->
    <div class="flex-1 ml-64">
        <main class="py-6">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <!-- Page Header -->
                <div class="mb-8">
                    <h1 class="text-2xl font-bold text-gray-900">Dashboard</h1>
                    <p class="text-gray-600">Overview of your hotel management system</p>
                </div>
                
                <!-- Quick Stats Cards -->
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                    <!-- Total Hotels -->
                    <div class="bg-white rounded-lg shadow p-6">
                        <div class="flex items-center">
                            <div class="p-3 rounded-full bg-blue-100">
                                <i class="fas fa-building text-blue-600 text-xl"></i>
                            </div>
                            <div class="ml-4">
                                <p class="text-sm font-medium text-gray-500">Total Hotels</p>
                                <p class="text-2xl font-semibold text-gray-900">${totalHotels}</p>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Total Rooms -->
                    <div class="bg-white rounded-lg shadow p-6">
                        <div class="flex items-center">
                            <div class="p-3 rounded-full bg-green-100">
                                <i class="fas fa-bed text-green-600 text-xl"></i>
                            </div>
                            <div class="ml-4">
                                <p class="text-sm font-medium text-gray-500">Total Rooms</p>
                                <p class="text-2xl font-semibold text-gray-900">${totalRooms}</p>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Available Rooms -->
                    <div class="bg-white rounded-lg shadow p-6">
                        <div class="flex items-center">
                            <div class="p-3 rounded-full bg-green-100">
                                <i class="fas fa-door-open text-green-600 text-xl"></i>
                            </div>
                            <div class="ml-4">
                                <p class="text-sm font-medium text-gray-500">Available Rooms</p>
                                <p class="text-2xl font-semibold text-gray-900">
                                    <c:out value="${roomStatusCounts['AVAILABLE'] != null ? roomStatusCounts['AVAILABLE'] : 0}" />
                                </p>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Total Bookings -->
                    <div class="bg-white rounded-lg shadow p-6">
                        <div class="flex items-center">
                            <div class="p-3 rounded-full bg-purple-100">
                                <i class="fas fa-calendar-check text-purple-600 text-xl"></i>
                            </div>
                            <div class="ml-4">
                                <p class="text-sm font-medium text-gray-500">Total Bookings</p>
                                <p class="text-2xl font-semibold text-gray-900">${totalBookings}</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Main Dashboard Content -->
                <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
                    <!-- Room Status Overview -->
                    <div class="bg-white rounded-lg shadow">
                        <div class="px-6 py-4 border-b border-gray-200">
                            <h3 class="text-lg font-medium text-gray-900">Room Status Overview</h3>
                        </div>
                        <div class="p-6">
                            <div class="space-y-4">
                                <c:forEach var="status" items="AVAILABLE,OCCUPIED,MAINTENANCE,CLEANING,OUT_OF_ORDER">
                                    <div class="flex items-center justify-between">
                                        <div class="flex items-center">
                                            <div class="w-3 h-3 rounded-full mr-3
                                                ${status == 'AVAILABLE' ? 'bg-green-500' : ''}
                                                ${status == 'OCCUPIED' ? 'bg-red-500' : ''}
                                                ${status == 'MAINTENANCE' ? 'bg-yellow-500' : ''}
                                                ${status == 'CLEANING' ? 'bg-blue-500' : ''}
                                                ${status == 'OUT_OF_ORDER' ? 'bg-gray-500' : ''}"></div>
                                            <span class="text-sm text-gray-600 capitalize">${status.toLowerCase().replace('_', ' ')}</span>
                                        </div>
                                        <span class="text-sm font-medium text-gray-900">
                                            <c:out value="${roomStatusCounts[status] != null ? roomStatusCounts[status] : 0}" />
                                        </span>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Booking Status Overview -->
                    <div class="bg-white rounded-lg shadow">
                        <div class="px-6 py-4 border-b border-gray-200">
                            <h3 class="text-lg font-medium text-gray-900">Booking Status Overview</h3>
                        </div>
                        <div class="p-6">
                            <div class="space-y-4">
                                <c:forEach var="status" items="CONFIRMED,CHECKED_IN,CHECKED_OUT,PENDING,CANCELLED">
                                    <div class="flex items-center justify-between">
                                        <div class="flex items-center">
                                            <div class="w-3 h-3 rounded-full mr-3
                                                ${status == 'CONFIRMED' ? 'bg-green-500' : ''}
                                                ${status == 'CHECKED_IN' ? 'bg-blue-500' : ''}
                                                ${status == 'CHECKED_OUT' ? 'bg-gray-500' : ''}
                                                ${status == 'PENDING' ? 'bg-yellow-500' : ''}
                                                ${status == 'CANCELLED' ? 'bg-red-500' : ''}"></div>
                                            <span class="text-sm text-gray-600 capitalize">${status.toLowerCase().replace('_', ' ')}</span>
                                        </div>
                                        <span class="text-sm font-medium text-gray-900">
                                            <c:out value="${bookingStatusCounts[status] != null ? bookingStatusCounts[status] : 0}" />
                                        </span>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Recent Activity -->
                <div class="mt-8 grid grid-cols-1 lg:grid-cols-2 gap-8">
                    <!-- Today's Check-ins -->
                    <div class="bg-white rounded-lg shadow">
                        <div class="px-6 py-4 border-b border-gray-200">
                            <h3 class="text-lg font-medium text-gray-900">Today's Check-ins</h3>
                        </div>
                        <div class="p-6">
                            <c:choose>
                                <c:when test="${not empty todayCheckIns}">
                                    <div class="space-y-4">
                                        <c:forEach var="booking" items="${todayCheckIns}" varStatus="status">
                                            <c:if test="${status.index < 5}">
                                                <div class="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                                    <div>
                                                        <p class="text-sm font-medium text-gray-900">${booking.customerName}</p>
                                                        <p class="text-xs text-gray-500">Room ${booking.room.roomNumber}</p>
                                                    </div>
                                                    <div class="text-right">
                                                        <p class="text-xs text-gray-500">${booking.guestCount} guests</p>
                                                        <p class="text-xs text-green-600">${booking.status}</p>
                                                    </div>
                                                </div>
                                            </c:if>
                                        </c:forEach>
                                    </div>
                                    <c:if test="${fn:length(todayCheckIns) > 5}">
                                        <div class="mt-4 text-center">
                                            <a href="/hotel-admin/bookings" class="text-sm text-blue-600 hover:text-blue-500">
                                                View all ${fn:length(todayCheckIns)} check-ins
                                            </a>
                                        </div>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <div class="text-center py-8">
                                        <i class="fas fa-calendar-day text-gray-300 text-3xl mb-2"></i>
                                        <p class="text-gray-500">No check-ins scheduled for today</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    
                    <!-- Today's Check-outs -->
                    <div class="bg-white rounded-lg shadow">
                        <div class="px-6 py-4 border-b border-gray-200">
                            <h3 class="text-lg font-medium text-gray-900">Today's Check-outs</h3>
                        </div>
                        <div class="p-6">
                            <c:choose>
                                <c:when test="${not empty todayCheckOuts}">
                                    <div class="space-y-4">
                                        <c:forEach var="booking" items="${todayCheckOuts}" varStatus="status">
                                            <c:if test="${status.index < 5}">
                                                <div class="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                                    <div>
                                                        <p class="text-sm font-medium text-gray-900">${booking.customerName}</p>
                                                        <p class="text-xs text-gray-500">Room ${booking.room.roomNumber}</p>
                                                    </div>
                                                    <div class="text-right">
                                                        <p class="text-xs text-gray-500">${booking.guestCount} guests</p>
                                                        <p class="text-xs text-orange-600">${booking.status}</p>
                                                    </div>
                                                </div>
                                            </c:if>
                                        </c:forEach>
                                    </div>
                                    <c:if test="${fn:length(todayCheckOuts) > 5}">
                                        <div class="mt-4 text-center">
                                            <a href="/hotel-admin/bookings" class="text-sm text-blue-600 hover:text-blue-500">
                                                View all ${fn:length(todayCheckOuts)} check-outs
                                            </a>
                                        </div>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <div class="text-center py-8">
                                        <i class="fas fa-sign-out-alt text-gray-300 text-3xl mb-2"></i>
                                        <p class="text-gray-500">No check-outs scheduled for today</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
                
                <!-- Quick Actions -->
                <div class="mt-8">
                    <div class="bg-white rounded-lg shadow">
                        <div class="px-6 py-4 border-b border-gray-200">
                            <h3 class="text-lg font-medium text-gray-900">Quick Actions</h3>
                        </div>
                        <div class="p-6">
                            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                                <a href="/hotel-admin/hotels/new" 
                                   class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors">
                                    <i class="fas fa-plus-circle text-2xl text-gray-400 mb-2"></i>
                                    <span class="text-sm text-gray-600">Add Hotel</span>
                                </a>
                                
                                <a href="/hotel-admin/rooms/new" 
                                   class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-green-500 hover:bg-green-50 transition-colors">
                                    <i class="fas fa-bed text-2xl text-gray-400 mb-2"></i>
                                    <span class="text-sm text-gray-600">Add Room</span>
                                </a>
                                
                                <a href="/hotel-admin/amenities" 
                                   class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-purple-500 hover:bg-purple-50 transition-colors">
                                    <i class="fas fa-swimming-pool text-2xl text-gray-400 mb-2"></i>
                                    <span class="text-sm text-gray-600">Manage Amenities</span>
                                </a>
                                
                                <a href="/hotel-admin/bookings" 
                                   class="flex flex-col items-center p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-orange-500 hover:bg-orange-50 transition-colors">
                                    <i class="fas fa-calendar-check text-2xl text-gray-400 mb-2"></i>
                                    <span class="text-sm text-gray-600">View Bookings</span>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        
        <%@ include file="layout/footer.jsp" %>
    </div>
</div>

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>