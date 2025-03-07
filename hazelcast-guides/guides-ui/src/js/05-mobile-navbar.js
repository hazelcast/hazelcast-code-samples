document.addEventListener('DOMContentLoaded', function () {
  var navbarToggles = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0)
  if (navbarToggles.length === 0) return
  navbarToggles.forEach(function (el) {
    el.addEventListener('click', function (e) {
      e.stopPropagation()
      el.classList.toggle('is-active')
      document.getElementById(el.dataset.target).classList.toggle('is-active')
      document.documentElement.classList.toggle('is-clipped--navbar')
    })
  })
})
