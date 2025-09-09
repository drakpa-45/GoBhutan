<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hotel Admin Signup</title>
    <!-- Main template CSS -->
    <link href="<c:url value='../../resources/dist/css/adminlte.min2167.css' />" rel="stylesheet">
    <link href="<c:url value='../../resources/dist/bootstrapv5/css/bootstrap.min.css' />" rel="stylesheet">
    <link href="<c:url value='../../resources/dist/bootstrapv5/css/bootstrap.css' />" rel="stylesheet">
</head>
<body class="hold-transition login-page bg-gray-100 flex items-center justify-center h-screen">

<form action="<c:url value='/auth/signup' />" method="post" class="bg-white p-8 rounded shadow-md w-96">
    <h2 class="text-2xl font-bold mb-6 text-center">Admin Signup</h2>

    <input type="text" name="username" placeholder="Username"
           class="border p-2 mb-4 w-full rounded"/>

    <input type="email" name="email" placeholder="Email"
           class="border p-2 mb-4 w-full rounded"/>

    <input type="password" name="password" placeholder="Password"
           class="border p-2 mb-4 w-full rounded"/>

    <button type="submit"
            class="bg-green-500 text-white py-2 rounded w-full hover:bg-green-600">
        Sign Up
    </button>

    <p class="mt-4 text-center text-sm">
        Already have an account?
        <a href="<c:url value='/login' />" class="text-blue-500 hover:underline">Login</a>
    </p>
</form>

<!-- ✅ AdminLTE JS (if required for components) -->
<script src="<c:url value='/resources/dist/js/adminlte.min.js' />"></script>
</body>
</html>
