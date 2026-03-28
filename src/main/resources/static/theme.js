(function () {
    var STORAGE_KEY = 'nexus-theme';

    function applyTheme(theme) {
        if (theme === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
        } else {
            document.documentElement.removeAttribute('data-theme');
        }
    }

    function getInitialTheme() {
        var savedTheme = localStorage.getItem(STORAGE_KEY);
        if (savedTheme === 'dark' || savedTheme === 'light') {
            return savedTheme;
        }

        var prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        return prefersDark ? 'dark' : 'light';
    }

    function setTheme(theme) {
        localStorage.setItem(STORAGE_KEY, theme);
        applyTheme(theme);
        window.dispatchEvent(new CustomEvent('nexus-theme-changed', { detail: { theme: theme } }));
    }

    function toggleTheme() {
        var currentTheme = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
        var nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
        setTheme(nextTheme);
        return nextTheme;
    }

    window.NexusTheme = {
        key: STORAGE_KEY,
        applyTheme: applyTheme,
        setTheme: setTheme,
        toggleTheme: toggleTheme,
        getCurrentTheme: function () {
            return document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
        }
    };

    applyTheme(getInitialTheme());
})();

