/**
 * Claudinary - Main Application JavaScript
 */

// Global configuration
const CLAUDINARY_CONFIG = {
    API_BASE_URL: '/api',
    UPLOAD_ENDPOINT: '/api/images/upload',
    MAX_FILE_SIZE: 10 * 1024 * 1024, // 10MB
    SUPPORTED_FORMATS: [
        'jpg', 'jpeg', 'png', 'gif', 'bmp', 'tiff', 'tif',
        'heic', 'heif', 'svg', 'ico', 'psd', 'pdf', 'webp'
    ]
};

// Authentication Manager
class AuthManager {
    static getToken() {
        return localStorage.getItem('token') || sessionStorage.getItem('token');
    }

    static setToken(token, rememberMe = false) {
        if (rememberMe) {
            localStorage.setItem('token', token);
        } else {
            sessionStorage.setItem('token', token);
        }
    }

    static removeToken() {
        localStorage.removeItem('token');
        sessionStorage.removeItem('token');
    }

    static getRefreshToken() {
        return localStorage.getItem('refreshToken') || sessionStorage.getItem('refreshToken');
    }

    static setRefreshToken(token, rememberMe = false) {
        if (rememberMe) {
            localStorage.setItem('refreshToken', token);
        } else {
            sessionStorage.setItem('refreshToken', token);
        }
    }

    static removeRefreshToken() {
        localStorage.removeItem('refreshToken');
        sessionStorage.removeItem('refreshToken');
    }

    static isAuthenticated() {
        return !!this.getToken();
    }

    static getAuthHeader() {
        const token = this.getToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }
}

// Utility Functions
const ClaudinaryUtils = {
    // Format file size
    formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    },

    // Get file extension
    getFileExtension(filename) {
        return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2).toLowerCase();
    },

    // Is supported format
    isSupportedFormat(filename) {
        const ext = this.getFileExtension(filename);
        return CLAUDINARY_CONFIG.SUPPORTED_FORMATS.includes(ext);
    },

    // Show notification
    showNotification(message, type = 'info', duration = 3000) {
        const toast = document.createElement('div');
        toast.className = `position-fixed top-0 end-0 p-3 bg-${type} text-white toast-style`;
        toast.style.zIndex = 10000;
        toast.style.right = '20px';
        toast.style.top = '20px';
        toast.style.minWidth = '280px';
        toast.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="bi bi-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'} me-2"></i>
                <div>${message}</div>
            </div>
        `;
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity 0.3s';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    },

    // Copy to clipboard
    copyToClipboard(text, successMessage = 'Copied to clipboard!') {
        navigator.clipboard.writeText(text).then(() => {
            this.showNotification(successMessage, 'success');
        }).catch(err => {
            this.showNotification('Failed to copy: ' + err.message, 'error');
        });
    },

    // Confirm action
    confirmAction(message, confirmText = 'Confirm', cancelText = 'Cancel') {
        return new Promise((resolve, reject) => {
            const modal = document.createElement('div');
            modal.className = 'modal fade';
            modal.innerHTML = `
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">Confirm Action</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <p>${message}</p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">${cancelText}</button>
                            <button type="button" class="btn btn-danger" id="confirmBtn">${confirmText}</button>
                        </div>
                    </div>
                </div>
            `;
            
            document.body.appendChild(modal);
            
            const bsModal = new bootstrap.Modal(modal);
            bsModal.show();
            
            const confirmBtn = modal.querySelector('#confirmBtn');
            confirmBtn.addEventListener('click', () => {
                bsModal.hide();
                modal.remove();
                resolve(true);
            });
            
            modal.addEventListener('hidden.bs.modal', () => {
                modal.remove();
                resolve(false);
            });
        });
    },

    // Format date
    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    // Format date for display
    formatDisplayDate(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60000) return 'Just now';
        if (diff < 3600000) return Math.floor(diff / 60000) + ' minutes ago';
        if (diff < 86400000) return Math.floor(diff / 3600000) + ' hours ago';
        if (diff < 604800000) return Math.floor(diff / 86400000) + ' days ago';
        return this.formatDate(dateString);
    }
};

// API Client
class ApiClient {
    constructor(baseUrl = CLAUDINARY_CONFIG.API_BASE_URL) {
        this.baseUrl = baseUrl;
    }

    async request(method, endpoint, data = null, headers = {}) {
        const url = this.baseUrl + endpoint;
        const authHeaders = AuthManager.getAuthHeader();
        
        const options = {
            method: method.toUpperCase(),
            headers: {
                'Content-Type': 'application/json',
                ...authHeaders,
                ...headers
            }
        };

        if (data && (method.toLowerCase() === 'post' || method.toLowerCase() === 'put' || method.toLowerCase() === 'patch')) {
            options.body = JSON.stringify(data);
        }

        try {
            const response = await fetch(url, options);
            
            if (!response.ok) {
                const error = await this._handleError(response);
                throw error;
            }

            // Try to parse JSON, otherwise return text
            try {
                return await response.json();
            } catch (e) {
                return await response.text();
            }
        } catch (error) {
            console.error('API request failed:', error);
            throw error;
        }
    }

    async _handleError(response) {
        try {
            const errorData = await response.json();
            return {
                status: response.status,
                message: errorData.message || errorData.error || 'Unknown error',
                data: errorData
            };
        } catch (e) {
            return {
                status: response.status,
                message: response.statusText || 'Request failed',
                data: null
            };
        }
    }

    // Convenience methods
    async get(endpoint, headers = {}) {
        return this.request('GET', endpoint, null, headers);
    }

    async post(endpoint, data = null, headers = {}) {
        return this.request('POST', endpoint, data, headers);
    }

    async put(endpoint, data = null, headers = {}) {
        return this.request('PUT', endpoint, data, headers);
    }

    async delete(endpoint, headers = {}) {
        return this.request('DELETE', endpoint, null, headers);
    }
}

// Image Upload Manager
class ImageUploadManager {
    constructor(apiClient) {
        this.apiClient = apiClient;
        this.dragAndDropSupported = 'draggable' in document.createElement('div');
    }

    setupUploadZone(uploadZoneId, fileInputId, previewContainerId, uploadButtonId) {
        const uploadZone = document.getElementById(uploadZoneId);
        const fileInput = document.getElementById(fileInputId);
        const previewContainer = document.getElementById(previewContainerId);
        const uploadButton = document.getElementById(uploadButtonId);

        if (!uploadZone || !fileInput || !previewContainer) {
            console.error('Upload zone elements not found');
            return;
        }

        let currentFile = null;

        // Handle drag and drop
        if (this.dragAndDropSupported) {
            uploadZone.addEventListener('dragover', (e) => {
                e.preventDefault();
                uploadZone.classList.add('dragover');
            });

            uploadZone.addEventListener('dragleave', () => {
                uploadZone.classList.remove('dragover');
            });

            uploadZone.addEventListener('drop', (e) => {
                e.preventDefault();
                uploadZone.classList.remove('dragover');
                if (e.dataTransfer.files.length > 0) {
                    this.handleFiles(e.dataTransfer.files, uploadZone, previewContainer, uploadButton);
                }
            });
        }

        // Handle click to browse
        uploadZone.addEventListener('click', () => {
            fileInput.click();
        });

        // Handle file selection
        fileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                this.handleFiles(e.target.files, uploadZone, previewContainer, uploadButton);
            }
        });

        // Store reference for external access
        uploadZone._fileInput = fileInput;
        uploadZone._previewContainer = previewContainer;
        uploadZone._uploadButton = uploadButton;
        uploadZone._currentFile = null;
    }

    handleFiles(files, uploadZone, previewContainer, uploadButton) {
        const file = files[0];
        
        // Validate file
        if (!ClaudinaryUtils.isSupportedFormat(file.name)) {
            ClaudinaryUtils.showNotification('Unsupported file format. Please upload a valid image.', 'error');
            return;
        }

        if (file.size > CLAUDINARY_CONFIG.MAX_FILE_SIZE) {
            ClaudinaryUtils.showNotification(`File size exceeds ${CLAUDINARY_CONFIG.MAX_FILE_SIZE / (1024 * 1024)}MB limit.`, 'error');
            return;
        }

        // Store current file
        uploadZone._currentFile = file;
        
        // Show preview
        this.showPreview(file, previewContainer);
        
        // Enable upload button
        if (uploadButton) {
            uploadButton.disabled = false;
        }
    }

    showPreview(file, previewContainer) {
        const reader = new FileReader();
        
        reader.onload = (e) => {
            const previewHtml = `
                <img id="imagePreview" src="${e.target.result}" alt="Preview" class="img-fluid" style="max-height: 300px; border-radius: 5px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                <div class="mt-2">
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="imageUploadManager.removeImage('${previewContainer.id}')">
                        <i class="bi bi-trash"></i> Remove
                    </button>
                </div>
            `;
            
            previewContainer.innerHTML = previewHtml;
            previewContainer.classList.add('show');
        };

        // Try to read as image
        try {
            reader.readAsDataURL(file);
        } catch (error) {
            // For non-image files, show file info
            previewContainer.innerHTML = `
                <div class="alert alert-info">
                    <i class="bi bi-file-earmark me-2"></i>
                    ${file.name} (${ClaudinaryUtils.formatFileSize(file.size)})
                </div>
            `;
            previewContainer.classList.add('show');
        }
    }

    removeImage(previewContainerId) {
        const previewContainer = document.getElementById(previewContainerId);
        const uploadZone = previewContainer.parentElement.querySelector('.upload-zone');
        const uploadButton = previewContainer.parentElement.querySelector('#uploadButton');
        
        if (previewContainer) {
            previewContainer.classList.remove('show');
            previewContainer.innerHTML = '';
        }
        
        if (uploadZone && uploadZone._fileInput) {
            uploadZone._fileInput.value = '';
        }
        
        if (uploadButton) {
            uploadButton.disabled = true;
        }
        
        uploadZone._currentFile = null;
    }

    async uploadImage(projectId = null) {
        const uploadZone = document.querySelector('.upload-zone');
        const fileInput = uploadZone ? uploadZone._fileInput : document.getElementById('fileInput');
        const titleInput = document.getElementById('title');
        const descriptionInput = document.getElementById('description');
        const isPublicInput = document.getElementById('isPublic');

        if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
            ClaudinaryUtils.showNotification('Please select a file first', 'error');
            return null;
        }

        const file = fileInput.files[0];
        
        const formData = new FormData();
        formData.append('file', file);
        
        if (titleInput && titleInput.value) {
            formData.append('title', titleInput.value);
        }
        
        if (descriptionInput && descriptionInput.value) {
            formData.append('description', descriptionInput.value);
        }
        
        if (isPublicInput) {
            formData.append('isPublic', isPublicInput.checked);
        }
        
        if (projectId) {
            formData.append('projectId', projectId);
        }

        try {
            const response = await fetch(CLAUDINARY_CONFIG.UPLOAD_ENDPOINT, {
                method: 'POST',
                headers: {
                    ...AuthManager.getAuthHeader()
                },
                body: formData
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Upload failed');
            }

            const result = await response.json();
            ClaudinaryUtils.showNotification('Image uploaded successfully!', 'success');
            
            // Reset form
            this.removeImage('previewContainer');
            if (titleInput) titleInput.value = '';
            if (descriptionInput) descriptionInput.value = '';
            
            return result;
        } catch (error) {
            ClaudinaryUtils.showNotification(error.message || 'Upload failed', 'error');
            throw error;
        }
    }
}

// DOM Content Loaded
let apiClient;
let imageUploadManager;

document.addEventListener('DOMContentLoaded', function() {
    // Initialize API client
    apiClient = new ApiClient();
    
    // Initialize image upload manager
    imageUploadManager = new ImageUploadManager(apiClient);
    
    // Setup upload zones
    const uploadZones = document.querySelectorAll('.upload-zone');
    uploadZones.forEach(zone => {
        const zoneId = zone.id || 'uploadZone';
        const fileInputId = zoneId + 'Input';
        const previewContainerId = zoneId + 'Preview';
        const uploadButtonId = zoneId + 'Button';
        
        imageUploadManager.setupUploadZone(zoneId, fileInputId, previewContainerId, uploadButtonId);
    });
    
    // Global AJAX error handling
    document.addEventListener('ajaxError', function(e) {
        const error = e.detail;
        if (error.status === 401) {
            // Unauthorized - redirect to login
            window.location.href = '/login';
        } else if (error.status === 403) {
            // Forbidden - show access denied
            window.location.href = '/access-denied';
        } else {
            ClaudinaryUtils.showNotification(error.message || 'An error occurred', 'error');
        }
    });
    
    // Initialize Bootstrap tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
});

// Make imageUploadManager available globally
window.imageUploadManager = imageUploadManager;
window.ClaudinaryUtils = ClaudinaryUtils;
window.AuthManager = AuthManager;

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        AuthManager,
        ClaudinaryUtils,
        ApiClient,
        ImageUploadManager
    };
}
