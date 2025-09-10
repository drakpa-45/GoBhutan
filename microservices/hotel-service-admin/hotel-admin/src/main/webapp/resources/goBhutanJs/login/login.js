$(document).ready(function () {
    $("#loginForm").on("submit", function (e) {
        e.preventDefault();

        const username = $("input[name='username']").val();
        const password = $("input[name='password']").val();

        $.ajax({
            url: pageContextPath + "/auth/signin", // dynamic base path
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                username: username,
                password: password
            }),
            success: function (response) {
                if (response.accessToken) {
                    alert("Login successful ✅");
                    // Store token in sessionStorage or localStorage
                    sessionStorage.setItem("access_token", response.accessToken);
                    sessionStorage.setItem("refresh_token", response.refreshToken);

                    // Redirect to dashboard (adjust URL)
                    window.location.href = `${pageContextPath}/auth/dashboard`;
                } else {
                    alert(response.message || "Invalid login attempt");
                }
            },
            error: function (xhr) {
                if (xhr.status === 401) {
                    alert("Invalid username or password ❌");
                } else if (xhr.status === 403) {
                    alert("Forbidden: You don't have access 🚫");
                } else {
                    alert("Server error: " + xhr.responseText);
                }
            }
        });
    });
});
