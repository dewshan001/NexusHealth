(function () {
    'use strict';

    function isValidEmail(email) {
        var value = (email || '').trim();
        if (!value) {
            return false;
        }
        return /^[A-Za-z0-9+_.-]+@(.+)$/.test(value);
    }

    function isValidPassword(password) {
        var value = password || '';
        return value.length >= 6 && value.length <= 128;
    }

    function isValidFullName(name) {
        var value = (name || '').trim();
        return value.length >= 3 && value.length <= 100;
    }

    function isValidPhone(phone) {
        var value = (phone || '').trim();
        return value.length >= 7;
    }

    function isValidAddress(address) {
        var value = (address || '').trim();
        return value.length >= 5;
    }

    function isValidGender(gender) {
        var value = (gender || '').trim().toLowerCase();
        return value === 'male' || value === 'female' || value === 'other';
    }

    function isValidBloodType(bloodType) {
        var allowed = ['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-'];
        return allowed.indexOf((bloodType || '').trim()) !== -1;
    }

    function showInlineMessage(container, message, type) {
        if (!container) {
            return;
        }

        container.hidden = false;
        container.textContent = message;
        container.classList.remove('auth-inline-message--error', 'auth-inline-message--success');
        container.classList.add(type === 'success' ? 'auth-inline-message--success' : 'auth-inline-message--error');
    }

    function clearInlineMessage(container) {
        if (!container) {
            return;
        }

        container.hidden = true;
        container.textContent = '';
    }

    function getFieldErrorElement(form, inputName) {
        return form.querySelector('.field-error[data-for="' + inputName + '"]');
    }

    function setFieldError(form, inputName, message) {
        var input = form.elements[inputName];
        if (!input) {
            return;
        }

        var errorElement = getFieldErrorElement(form, inputName);
        var wrapper = input.closest('.login-input-wrap');

        if (message) {
            input.setAttribute('aria-invalid', 'true');
            if (wrapper) {
                wrapper.classList.add('is-invalid');
            }
            if (errorElement) {
                errorElement.textContent = message;
                errorElement.style.color = '#b91c1c';
            }
            return;
        }

        input.removeAttribute('aria-invalid');
        if (wrapper) {
            wrapper.classList.remove('is-invalid');
        }
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.style.color = '#b91c1c';
        }
    }

    function createModalController() {
        var modal = document.getElementById('authModal');
        if (!modal) {
            return {
                show: function () {}
            };
        }

        var titleEl = document.getElementById('authModalTitle');
        var messageEl = document.getElementById('authModalMessage');

        function closeModal() {
            modal.classList.remove('is-open', 'auth-modal--error', 'auth-modal--success');
            modal.setAttribute('aria-hidden', 'true');
            document.body.classList.remove('auth-modal-open');
        }

        function showModal(message, type, title) {
            if (!message) {
                return;
            }

            titleEl.textContent = title || (type === 'success' ? 'Success' : 'Sign-in failed');
            messageEl.textContent = message;
            modal.classList.add('is-open');
            modal.classList.remove('auth-modal--error', 'auth-modal--success');
            modal.classList.add(type === 'success' ? 'auth-modal--success' : 'auth-modal--error');
            modal.setAttribute('aria-hidden', 'false');
            document.body.classList.add('auth-modal-open');
        }

        modal.querySelectorAll('[data-auth-modal-close]').forEach(function (closeTrigger) {
            closeTrigger.addEventListener('click', closeModal);
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && modal.classList.contains('is-open')) {
                closeModal();
            }
        });

        return {
            show: showModal,
            close: closeModal
        };
    }

    function validateLoginField(form, fieldName) {
        var value = form.elements[fieldName] ? form.elements[fieldName].value : '';

        if (fieldName === 'email') {
            if (!value || !value.trim()) {
                return 'Email is required';
            }
            if (!isValidEmail(value)) {
                return 'Please enter a valid email address';
            }
        }

        if (fieldName === 'password') {
            if (!value) {
                return 'Password is required';
            }
            if (!isValidPassword(value)) {
                return 'Password must be between 6 and 128 characters';
            }
        }

        return '';
    }

    function validateSignupField(form, fieldName) {
        var field = form.elements[fieldName];
        var value = field ? field.value : '';

        switch (fieldName) {
            case 'fullName':
                if (!value || !value.trim()) {
                    return 'Full name is required';
                }
                if (!isValidFullName(value)) {
                    return 'Full name must be 3 to 100 characters';
                }
                return '';
            case 'email':
                if (!value || !value.trim()) {
                    return 'Email is required';
                }
                if (!isValidEmail(value)) {
                    return 'Please enter a valid email address';
                }
                return '';
            case 'password':
                if (!value) {
                    return 'Password is required';
                }
                if (!isValidPassword(value)) {
                    return 'Password must be between 6 and 128 characters';
                }
                return '';
            case 'phone':
                if (!value || !value.trim()) {
                    return 'Phone number is required';
                }
                if (!isValidPhone(value)) {
                    return 'Phone number must have at least 7 characters';
                }
                return '';
            case 'dateOfBirth':
                if (!value) {
                    return 'Date of birth is required';
                }
                return '';
            case 'gender':
                if (!isValidGender(value)) {
                    return 'Please select a valid gender';
                }
                return '';
            case 'bloodType':
                if (!isValidBloodType(value)) {
                    return 'Please select a valid blood type';
                }
                return '';
            case 'address':
                if (!value || !value.trim()) {
                    return 'Address is required';
                }
                if (!isValidAddress(value)) {
                    return 'Address must be at least 5 characters';
                }
                return '';
            default:
                return '';
        }
    }

    function bindValidation(form, fieldNames, validateField, modalController) {
        fieldNames.forEach(function (fieldName) {
            var field = form.elements[fieldName];
            if (!field) {
                return;
            }

            var validateAndRender = function () {
                setFieldError(form, fieldName, validateField(form, fieldName));
            };

            field.addEventListener('input', validateAndRender);
            field.addEventListener('blur', validateAndRender);
            field.addEventListener('change', validateAndRender);
        });

        form.addEventListener('submit', function (event) {
            var isValid = true;
            var firstError = '';

            fieldNames.forEach(function (fieldName) {
                var fieldError = validateField(form, fieldName);
                setFieldError(form, fieldName, fieldError);
                if (fieldError) {
                    isValid = false;
                    if (!firstError) {
                        firstError = fieldError;
                    }
                }
            });

            if (!isValid) {
                event.preventDefault();
                if (modalController && typeof modalController.show === 'function') {
                    modalController.show(firstError || 'Please correct the highlighted form fields.', 'error', 'Validation error');
                }
            }
        });
    }

    function consumeFlashMessages(modalController, errorContainer, successContainer) {
        var handled = false;
        var flashes = document.querySelectorAll('.auth-flash-data[data-message]');
        flashes.forEach(function (flashElement) {
            var message = (flashElement.dataset.message || '').trim();
            var type = flashElement.dataset.type === 'success' ? 'success' : 'error';
            if (!message) {
                return;
            }

            showInlineMessage(type === 'success' ? successContainer : errorContainer, message, type);
            modalController.show(message, type, type === 'success' ? 'Success' : 'Sign-in failed');
            handled = true;
        });

        return handled;
    }

    function consumeQueryAuthError(modalController, errorContainer) {
        if (!window.location || !window.location.search) {
            return false;
        }

        var params = new URLSearchParams(window.location.search);
        var code = params.get('authError');
        if (!code) {
            return false;
        }

        var messageMap = {
            invalidEmail: 'Please enter a valid email address',
            invalidPassword: 'Password must be between 6-128 characters',
            invalidCredentials: 'Invalid email or password'
        };

        var message = messageMap[code] || 'Invalid email or password';
        showInlineMessage(errorContainer, message, 'error');
        modalController.show(message, 'error', 'Sign-in failed');

        params.delete('authError');
        var newSearch = params.toString();
        var newUrl = window.location.pathname + (newSearch ? ('?' + newSearch) : '') + (window.location.hash || '');
        window.history.replaceState({}, document.title, newUrl);
        return true;
    }

    document.addEventListener('DOMContentLoaded', function () {
        var loginForm = document.getElementById('loginForm');
        var signupForm = document.getElementById('signupForm');
        var errorContainer = document.getElementById('errorMessage');
        var successContainer = document.getElementById('successMessage');

        clearInlineMessage(errorContainer);
        clearInlineMessage(successContainer);

        var modalController = createModalController();
        var flashHandled = consumeFlashMessages(modalController, errorContainer, successContainer);
        if (!flashHandled) {
            consumeQueryAuthError(modalController, errorContainer);
        }

        if (loginForm) {
            bindValidation(loginForm, ['email', 'password'], validateLoginField, modalController);
        }

        if (signupForm) {
            bindValidation(
                signupForm,
                ['fullName', 'email', 'password', 'phone', 'dateOfBirth', 'gender', 'bloodType', 'address'],
                validateSignupField,
                modalController
            );
        }
    });
})();
