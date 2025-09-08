<!-- header.jsp -->
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hotel Admin Panel</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/alpinejs/3.x.x/cdn.js" defer></script>
</head>
<body class="bg-gray-50">
    <!-- Navigation Header -->
    <nav class="bg-white shadow-lg border-b">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between h-16">
                <div class="flex items-center">
                    <div class="flex-shrink-0">
                        <h1 class="text-xl font-bold text-gray-900">
                            <i class="fas fa-hotel text-blue-600 mr-2"></i>
                            Hotel Admin Panel
                        </h1>
                    </div>
                </div>
                
                <div class="flex items-center space-x-4">
                    <!-- Notifications -->
                    <button class="text-gray-500 hover:text-gray-700 p-2">
                        <i class="fas fa-bell text-lg"></i>
                        <span class="absolute -mt-1 ml-2 px-1 py-0.5 text-xs bg-red-500 text-white rounded-full">3</span>
                    </button>
                    
                    <!-- User Menu -->
                    <div class="relative" x-data="{ open: false }">
                        <button @click="open = !open" class="flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500">
                            <div class="h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center">
                                <i class="fas fa-user text-white text-sm"></i>
                            </div>
                            <span class="ml-2 text-gray-700 font-medium">
                                <sec:authentication property="principal.keycloakSecurityContext.token.preferredUsername" var="username"/>
                                ${username}
                            </span>
                            <i class="fas fa-chevron-down text-gray-500 text-xs ml-1"></i>
                        </button>
                        
                        <div x-show="open" @click.away="open = false" 
                             class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50">
                            <a href="/hotel-admin/profile" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                                <i class="fas fa-user-cog mr-2"></i>Profile Settings
                            </a>
                            <hr class="my-1">
                            <form action="/hotel-admin/logout" method="post" class="block">
                                <button type="submit" class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100">
                                    <i class="fas fa-sign-out-alt mr-2"></i>Logout
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </nav>

<!-- sidebar.jsp -->
<aside class="w-64 bg-white shadow-lg h-screen fixed left-0 top-16 overflow-y-auto">
    <nav class="mt-5 px-2">
        <div class="space-y-1">
            <!-- Dashboard -->
            <a href="/hotel-admin/" 
               class="flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors
                      ${pageContext.request.requestURI.contains('/dashboard') || pageContext.request.requestURI.equals('/hotel-admin/') ? 
                        'bg-blue-100 text-blue-900 border-r-4 border-blue-600' : 
                        'text-gray-600 hover:bg-gray-50 hover:text-gray-900'}">
                <i class="fas fa-tachometer-alt mr-3 text-lg"></i>
                Dashboard
            </a>
            
            <!-- Hotels -->
            <a href="/hotel-admin/hotels" 
               class="flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors
                      ${pageContext.request.requestURI.contains('/hotels') ? 
                        'bg-blue-100 text-blue-900 border-r-4 border-blue-600' : 
                        'text-gray-600 hover:bg-gray-50 hover:text-gray-900'}">
                <i class="fas fa-building mr-3 text-lg"></i>
                My Hotels
            </a>
            
            <!-- Rooms -->
            <a href="/hotel-admin/rooms" 
               class="flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors
                      ${pageContext.request.requestURI.contains('/rooms') ? 
                        'bg-blue-100 text-blue-900 border-r-4 border-blue-600' : 
                        'text-gray-600 hover:bg-gray-50 hover:text-gray-900'}">
                <i class="fas fa-bed mr-3 text-lg"></i>
                Room Management
            </a>
            
            <!-- Bookings -->
            <a href="/hotel-admin/bookings" 
               class="flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors
                      ${pageContext.request.requestURI.contains('/bookings') ? 
                        'bg-blue-100 text-blue-900 border-r-4 border-blue-600' : 
                        'text-gray-600 hover:bg-gray-50 hover:text-gray-900'}">
                <i class="fas fa-calendar-check mr-3 text-lg"></i>
                Booking Overview
            </a>
            
            <!-- Amenities -->
            <a href="/hotel-admin/amenities" 
               class="flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors
                      ${pageContext.request.requestURI.contains('/amenities') ? 
                        'bg-blue-100 text-blue-900 border-r-4 border-blue-600' : 
                        'text-gray-600 hover:bg-gray-50 hover:text-gray-900'}">
                <i class="fas fa-swimming-pool mr-3 text-lg"></i>
                Amenities
            </a>
            
            <!-- Reports (Future) -->
            <a href="#" class="flex items-center px-2 py-2 text-sm font-medium text-gray-400 cursor-not-allowed">
                <i class="fas fa-chart-bar mr-3 text-lg"></i>
                Reports
                <span class="ml-auto text-xs bg-gray-200 text-gray-600 px-2 py-1 rounded">Soon</span>
            </a>
        </div>
        
        <!-- Hotel Selection (if multiple hotels) -->
        <div class="mt-8 px-2">
            <h3 class="px-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">Quick Actions</h3>
            <div class="mt-2 space-y-1">
                <a href="/hotel-admin/hotels/new" 
                   class="flex items-center px-2 py-2 text-sm text-green-600 hover:bg-green-50 rounded-md">
                    <i class="fas fa-plus mr-3"></i>Add New Hotel
                </a>
                <a href="/hotel-admin/rooms/new" 
                   class="flex items-center px-2 py-2 text-sm text-blue-600 hover:bg-blue-50 rounded-md">
                    <i class="fas fa-plus mr-3"></i>Add New Room
                </a>
            </div>
        </div>
    </nav>
</aside>

<!-- footer.jsp -->
<footer class="bg-white border-t mt-auto py-4">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center">
            <div class="text-sm text-gray-500">
                © 2025 Hotel Admin Panel. All rights reserved.
            </div>
            <div class="text-sm text-gray-500">
                Version 1.0.0
            </div>
        </div>
    </div>
</footer>

<!-- Success/Error Messages -->
<c:if test="${not empty successMessage}">
    <div x-data="{ show: true }" x-show="show" x-init="setTimeout(() => show = false, 5000)"
         class="fixed top-20 right-4 bg-green-500 text-white px-6 py-3 rounded-md shadow-lg z-50">
        <div class="flex items-center">
            <i class="fas fa-check-circle mr-2"></i>
            ${successMessage}
            <button @click="show = false" class="ml-4 text-white hover:text-gray-200">
                <i class="fas fa-times"></i>
            </button>
        </div>
    </div>
</c:if>

<c:if test="${not empty errorMessage}">
    <div x-data="{ show: true }" x-show="show" x-init="setTimeout(() => show = false, 7000)"
         class="fixed top-20 right-4 bg-red-500 text-white px-6 py-3 rounded-md shadow-lg z-50">
        <div class="flex items-center">
            <i class="fas fa-exclamation-circle mr-2"></i>
            ${errorMessage}
            <button @click="show = false" class="ml-4 text-white hover:text-gray-200">
                <i class="fas fa-times"></i>
            </button>
        </div>
    </div>
</c:if>

<script>
    // Global JavaScript utilities
    function confirmDelete(message = 'Are you sure you want to delete this item?') {
        return confirm(message);
    }
    
    function showLoading(button) {
        const original = button.innerHTML;
        button.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Processing...';
        button.disabled = true;
        
        setTimeout(() => {
            button.innerHTML = original;
            button.disabled = false;
        }, 3000);
    }
    
    // Room status update function
    function updateRoomStatus(roomId, status) {
        fetch(`/hotel-admin/rooms/${roomId}/status`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `status=${status}`
        })
        .then(response => response.text())
        .then(data => {
            if (data === 'success') {
                location.reload();
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to update room status');
        });
    }
</script>
</body>
</html>