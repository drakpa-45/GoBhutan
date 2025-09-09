<!-- Custom JavaScript -->

    // Update current date
    document.getElementById('currentDate').textContent = new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
});

    // Handle dropdown submenus
    document.addEventListener('DOMContentLoaded', function() {
    // Add submenu support
    const dropdownSubmenus = document.querySelectorAll('.dropdown-submenu');

    dropdownSubmenus.forEach(function(submenu) {
    submenu.addEventListener('mouseenter', function() {
    const submenuDropdown = this.querySelector('.dropdown-menu');
    if (submenuDropdown) {
    submenuDropdown.style.display = 'block';
}
});

    submenu.addEventListener('mouseleave', function() {
    const submenuDropdown = this.querySelector('.dropdown-menu');
    if (submenuDropdown) {
    submenuDropdown.style.display = 'none';
}
});
});

    // Add animation to stats cards
    const statsCards = document.querySelectorAll('.stats-card');
    statsCards.forEach((card, index) => {
    card.style.animationDelay = `${index * 0.1}s`;
});
});

    // Search functionality placeholder
    document.querySelector('.search-container input').addEventListener('input', function(e) {
    const searchTerm = e.target.value;
    if (searchTerm.length > 2) {
    // Implement search logic here
    console.log('Searching for:', searchTerm);
}
});

    // Quick action buttons
    document.querySelectorAll('.btn-outline-primary, .btn-outline-success, .btn-outline-info, .btn-outline-warning, .btn-outline-secondary, .btn-outline-dark').forEach(button => {
    button.addEventListener('click', function() {
        const action = this.querySelector('span').textContent;
        alert(`${action} functionality would be implemented here`);
    });
});