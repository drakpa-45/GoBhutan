<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hotel Admin Signup</title>
    <jsp:include page="/resources/gobhutanCss.jsp"/>
    <link rel="stylesheet" href="<c:url value="../../../resources/goBhutanCss/signup/signup.css"/>"/>
</head>
<body class="hold-transition login-page">
<div class="login-box">
    <div class="login-logo">
        <b>Hotel</b>Admin
    </div>
    <div class="card">
        <div class="card-body login-card-body">
            <p class="login-box-msg">Create your admin account</p>

            <form id="signupForm" action="${pageContext.request.contextPath}/auth/signup" method="post">

                <!-- First Name -->
                <div class="input-group mb-3">
                    <input type="text" name="firstName" class="form-control" placeholder="First Name" required>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <span class="fas fa-id-badge"></span>
                        </div>
                    </div>
                </div>

                <!-- Last Name -->
                <div class="input-group mb-3">
                    <input type="text" name="lastName" class="form-control" placeholder="Last Name" required>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <span class="fas fa-id-badge"></span>
                        </div>
                    </div>
                </div>

                <!-- Username -->
                <div class="input-group mb-3">
                    <input type="text" name="username" class="form-control" placeholder="Username" required>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <span class="fas fa-user"></span>
                        </div>
                    </div>
                </div>

                <!-- Email -->
                <div class="input-group mb-3">
                    <input type="email" name="email" class="form-control" placeholder="Email" required>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <span class="fas fa-envelope"></span>
                        </div>
                    </div>
                </div>

                <!-- Password -->
                <div class="input-group mb-3">
                    <input type="password" id="password" name="password" class="form-control" placeholder="Password" required minlength="6">
                    <div class="input-group-append">
                        <div class="input-group-text toggle-password" data-target="#password">
                            <span class="fas fa-eye"></span>
                        </div>
                    </div>
                </div>

                <!-- Confirm Password -->
                <div class="input-group mb-1">
                    <input type="password" id="confirmPassword" class="form-control" placeholder="Confirm Password" required minlength="6">
                    <div class="input-group-append">
                        <div class="input-group-text toggle-password" data-target="#confirmPassword">
                            <span class="fas fa-eye"></span>
                        </div>
                    </div>
                </div>
                <p id="passwordError" class="error-msg">Passwords do not match!</p>

                <div class="row mt-3">
                    <div class="col-8">
                        <a href="${pageContext.request.contextPath}/auth/login">Already have an account? Login</a>
                    </div>
                    <div class="col-4">
                        <button type="submit" class="btn btn-success btn-block">Sign Up</button>
                    </div>
                </div>
            </form>

        </div>
    </div>
</div>
<jsp:include page="/resources/gobhutanJs.jsp"/>
<script type="text/javascript" src="<c:url value="../../../resources/goBhutanJs/signup/signup.js"/>"></script>
</body>
</html>
