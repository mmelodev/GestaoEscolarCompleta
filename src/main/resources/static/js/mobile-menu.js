(() => {
  function initMobileMenu() {
    const menuToggle = document.getElementById('menuToggle');
    const navList = document.getElementById('navList');
    const menuOverlay = document.getElementById('menuOverlay');
    const body = document.body;

    if (!menuToggle || !navList) return;
    if (menuToggle.dataset.mobileMenuInit === '1') return;
    menuToggle.dataset.mobileMenuInit = '1';

    let scrollYBeforeMenu = 0;

    function lockPageScroll() {
      scrollYBeforeMenu = window.pageYOffset || document.documentElement.scrollTop || 0;
      body.dataset.scrollYBeforeMenu = String(scrollYBeforeMenu);
      body.style.position = 'fixed';
      body.style.top = `-${scrollYBeforeMenu}px`;
      body.style.left = '0';
      body.style.right = '0';
      body.style.width = '100%';
      body.style.overflow = 'hidden';
    }

    function unlockPageScroll() {
      const y = parseInt(body.dataset.scrollYBeforeMenu || '0', 10) || 0;
      body.style.position = '';
      body.style.top = '';
      body.style.left = '';
      body.style.right = '';
      body.style.width = '';
      body.style.overflow = '';
      delete body.dataset.scrollYBeforeMenu;
      window.scrollTo(0, y);
    }

    function openMenu() {
      navList.classList.add('menu-open');
      menuToggle.classList.add('active');
      menuToggle.setAttribute('aria-expanded', 'true');
      if (menuOverlay) {
        menuOverlay.classList.add('active');
        menuOverlay.setAttribute('aria-hidden', 'false');
      }
      if (window.innerWidth < 1024) lockPageScroll();
    }

    function closeMenu() {
      navList.classList.remove('menu-open');
      menuToggle.classList.remove('active');
      menuToggle.setAttribute('aria-expanded', 'false');
      if (menuOverlay) {
        menuOverlay.classList.remove('active');
        menuOverlay.setAttribute('aria-hidden', 'true');
      }
      unlockPageScroll();
    }

    function toggleMenu() {
      const isOpen = navList.classList.contains('menu-open');
      if (isOpen) closeMenu();
      else openMenu();
    }

    menuToggle.addEventListener('click', (e) => {
      e.stopPropagation();
      toggleMenu();
    });

    if (menuOverlay) {
      menuOverlay.addEventListener('click', closeMenu);
    }

    navList.querySelectorAll('a').forEach((link) => {
      link.addEventListener('click', () => {
        if (window.innerWidth < 1024) closeMenu();
      });
    });

    const logoutButton = navList.querySelector('.logout-button');
    if (logoutButton) {
      logoutButton.addEventListener('click', () => {
        if (window.innerWidth < 1024) closeMenu();
      });
    }

    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && navList.classList.contains('menu-open')) {
        closeMenu();
        menuToggle.focus();
      }
    });

    let resizeTimer;
    window.addEventListener('resize', () => {
      clearTimeout(resizeTimer);
      resizeTimer = setTimeout(() => {
        if (window.innerWidth >= 1024 && navList.classList.contains('menu-open')) {
          closeMenu();
        }
      }, 250);
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initMobileMenu);
  } else {
    initMobileMenu();
  }
})();
