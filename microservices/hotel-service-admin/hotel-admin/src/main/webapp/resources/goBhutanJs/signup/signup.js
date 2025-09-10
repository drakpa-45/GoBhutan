const signupForm = document.getElementById("signupForm");
const password = document.getElementById("password");
const confirmPassword = document.getElementById("confirmPassword");
const errorMsg = document.getElementById("passwordError");

const contextPath = "${pageContext.request.contextPath}";

// Validate on form submit
signupForm.addEventListener("submit", function (e) {
    e.preventDefault(); // prevent default form submit

    // Check password match
    if (password.value !== confirmPassword.value) {
        errorMsg.style.display = "block";
        return;
    }

    // Collect form data
    const formData = {
        firstName: signupForm.firstName.value,
        lastName: signupForm.lastName.value,
        username: signupForm.username.value,
        email: signupForm.email.value,
        password: password.value
    };

    // Send AJAX request to backend
    fetch(`${signupForm.action}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
    })
        .then(async (res) => {
            if (res.status === 201) {
                alert("Signup successful! Please login.");
                window.location.href = `${window.location.origin}/auth/login`;
            } else {
                const data = await res.text();
                alert("Signup failed: " + data);
            }
        })
        .catch(err => {
            console.error(err);
            alert("Error connecting to server.");
        });
});
function successMsg(msg, url) {
    if (!url) {
        swal({
            title: "Success!",
            text: msg,
            icon: "success"
        });
    } else {
        swal({
            title: "Success!",
            text: msg,
            icon: "success"
        }, function() {
            window.location = url; // redirect after user clicks OK
        });
    }
}


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
