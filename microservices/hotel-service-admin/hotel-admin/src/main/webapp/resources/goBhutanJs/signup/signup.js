const signupForm = document.getElementById("signupForm");
const password = document.getElementById("password");
const confirmPassword = document.getElementById("confirmPassword");
const errorMsg = document.getElementById("passwordError");

// Validate on form submit
signupForm.addEventListener("submit", function (e) {
    if (password.value !== confirmPassword.value) {
        e.preventDefault();
        errorMsg.style.display = "block";
    }
});

// Real-time validation
confirmPassword.addEventListener("input", function () {
    if (password.value === confirmPassword.value) {
        errorMsg.style.display = "none";
    }
});
confirmPassword.addEventListener("focus", function () {
    errorMsg.style.display = "none";
});

// Toggle password visibility
document.querySelectorAll(".toggle-password").forEach(function (eyeIcon) {
    eyeIcon.addEventListener("click", function () {
        const target = document.querySelector(this.getAttribute("data-target"));
        const icon = this.querySelector("span");

        if (target.type === "password") {
            target.type = "text";
            icon.classList.remove("fa-eye");
            icon.classList.add("fa-eye-slash");
        } else {
            target.type = "password";
            icon.classList.remove("fa-eye-slash");
            icon.classList.add("fa-eye");
        }
    });
});