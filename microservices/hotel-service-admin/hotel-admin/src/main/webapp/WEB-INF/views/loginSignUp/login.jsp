<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hotel Admin Login</title>

    <jsp:include page="/resources/gobhutanCss.jsp"/>
    <link rel="stylesheet" href="<c:url value="../../../resources/goBhutanCss/login/login.css"/>"/>
</head>
<body class="hold-transition login-page">
<div class="login-box">
    <div class="login-logo">
        <b>Hotel</b>Admin
    </div>
    <div class="card">
        <div class="card-body login-card-body">
            <p class="login-box-msg">Sign in to start your session</p>

            <form action="${pageContext.request.contextPath}/auth/signin" method="post">
                <div class="input-group mb-3">
                    <input type="text" name="username" class="form-control" placeholder="Username" required>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <span class="fas fa-user"></span>
                        </div>
                    </div>
                </div>
                <div class="input-group mb-3">
                    <input type="password" name="password" class="form-control" placeholder="Password" required>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <span class="fas fa-lock"></span>
                        </div>
                    </div>
                </div>
                <div class="row mb-3">
                    <div class="col-8">
                        <a href="${pageContext.request.contextPath}/auth/forgot-password">Forgot password?</a>
                    </div>
                    <div class="col-4">
                        <button type="submit" class="btn btn-primary btn-block">Login</button>
                    </div>
                </div>
            </form>

            <!-- Sign up link -->
            <div class="extra-links">
                <span>Don't have an account?</span>
                <a href="${pageContext.request.contextPath}/auth/signup">Sign up</a>
            </div>

        </div>
    </div>
</div>
<jsp:include page="/resources/gobhutanJs.jsp"/>
<script type="text/javascript" src="<c:url value="../../../resources/goBhutanJs/login/login.js"/>"></script>
</body>
</html>
