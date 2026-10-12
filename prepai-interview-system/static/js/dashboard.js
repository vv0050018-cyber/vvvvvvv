// PREPAI Dashboard Interactive Visuals
document.addEventListener("DOMContentLoaded", () => {
  // Animate mini bars on dashboard load
  const bars = document.querySelectorAll(".mini-bar");
  bars.forEach(bar => {
    const targetHeight = bar.style.height;
    bar.style.height = "4px";
    setTimeout(() => {
      bar.style.height = targetHeight;
    }, 150);
  });
});
