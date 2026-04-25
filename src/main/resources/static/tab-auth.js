(function () {
  'use strict';

  var STORAGE_KEY = 'nexus.tabToken';

  function parseHashToken() {
    var hash = window.location.hash || '';
    if (!hash || hash.length < 2) return null;

    // Support either '#tabToken=...' or '#...&tabToken=...'
    var params = new URLSearchParams(hash.substring(1));
    var token = params.get('tabToken');
    if (!token) return null;

    try {
      sessionStorage.setItem(STORAGE_KEY, token);
    } catch (e) {
      // ignore
    }

    // Remove hash from the URL (so token doesn't linger in copy/paste)
    try {
      window.history.replaceState(null, document.title, window.location.pathname + window.location.search);
    } catch (e2) {
      // ignore
    }

    return token;
  }

  function getToken() {
    try {
      return sessionStorage.getItem(STORAGE_KEY);
    } catch (e) {
      return null;
    }
  }

  function clearToken() {
    try {
      sessionStorage.removeItem(STORAGE_KEY);
    } catch (e) {
      // ignore
    }
  }

  function shouldAttachToken(urlObj) {
    if (!urlObj) return false;
    if (urlObj.origin !== window.location.origin) return false;
    return urlObj.pathname.indexOf('/api/') === 0;
  }

  // Bootstrap token from URL fragment on first load after login
  parseHashToken();

  // If user opens a dashboard directly without a token, send them to login
  try {
    if (window.location.pathname.indexOf('dashboard') !== -1 && !getToken()) {
      window.location.href = '/login';
      return;
    }
  } catch (e) {
    // ignore
  }

  // Patch fetch() to automatically attach X-Tab-Token for /api/*
  var originalFetch = window.fetch;
  if (typeof originalFetch === 'function') {
    window.fetch = function (input, init) {
      var urlString = null;
      if (typeof input === 'string') {
        urlString = input;
      } else if (input && typeof input.url === 'string') {
        urlString = input.url;
      }

      var urlObj = null;
      try {
        if (urlString) urlObj = new URL(urlString, window.location.origin);
      } catch (e) {
        urlObj = null;
      }

      var token = getToken();
      var needsToken = token && shouldAttachToken(urlObj);

      if (needsToken) {
        init = init || {};

        var headers = new Headers(init.headers || (input && input.headers) || undefined);
        if (!headers.has('X-Tab-Token')) {
          headers.set('X-Tab-Token', token);
        }
        init.headers = headers;
      }

      return originalFetch(input, init).then(function (res) {
        if (res && (res.status === 401 || res.status === 403)) {
          // Token likely expired/revoked; keep behavior simple.
          clearToken();
          if (window.location.pathname.indexOf('dashboard') !== -1) {
            window.location.href = '/login';
          }
          return res;
        }

        // Some endpoints currently return HTTP 200 with a JSON body like:
        //   { success:false, message:"Unauthorized ..." }
        // If we attached a token and the body indicates auth failure, treat it like 401.
        try {
          if (needsToken && res && res.ok) {
            var contentType = res.headers && res.headers.get && res.headers.get('content-type');
            if (contentType && contentType.indexOf('application/json') !== -1) {
              res.clone().json().then(function (body) {
                if (!body || body.success !== false) return;

                var msg = (body.message || body.error || '').toString();
                var status = body.status;
                var isUnauthorized =
                  status === 401 ||
                  status === 403 ||
                  /unauthorized|authentication required|forbidden/i.test(msg);

                if (isUnauthorized) {
                  clearToken();
                  if (window.location.pathname.indexOf('dashboard') !== -1) {
                    window.location.href = '/login';
                  }
                }
              }).catch(function () {
                // ignore JSON parse errors
              });
            }
          }
        } catch (e3) {
          // ignore
        }
        return res;
      });
    };
  }

  // Expose tiny helpers (optional)
  window.__nexusTabAuth = {
    getToken: getToken,
    clearToken: clearToken
  };
})();
