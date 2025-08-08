/**
 * Task Manager V2 - Frontend JavaScript
 * A comprehensive task management application with modern UI/UX
 */

class TaskManager {
    constructor() {
        this.apiBaseUrl = 'http://localhost:8080/api';
        this.tasks = [];
        this.currentFilter = 'all';
        this.editingTaskId = null;
        this.debounceTimer = null;
        
        this.init();
    }

    /**
     * Initialize the application
     */
    async init() {
        this.setupEventListeners();
        this.setupFormValidation();
        await this.loadTasks();
        await this.updateStatistics();
        this.showToast('Welcome to Task Manager V2!', 'info');
    }

    /**
     * Setup event listeners for UI interactions
     */
    setupEventListeners() {
        // Form submission
        document.getElementById('taskForm').addEventListener('submit', (e) => this.handleFormSubmit(e));
        
        // Cancel edit button
        document.getElementById('cancelEdit').addEventListener('click', () => this.cancelEdit());
        
        // Search input
        document.getElementById('searchInput').addEventListener('input', (e) => this.handleSearch(e.target.value));
        
        // Filter buttons
        document.querySelectorAll('.btn-filter').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleFilter(e.target.dataset.filter));
        });
        
        // Error banner close
        document.getElementById('closeError').addEventListener('click', () => this.hideError());
        
        // Character counter for description
        document.getElementById('taskDescription').addEventListener('input', (e) => this.updateCharCount(e.target));
    }

    /**
     * Setup form validation
     */
    setupFormValidation() {
        const titleInput = document.getElementById('taskTitle');
        const descInput = document.getElementById('taskDescription');
        
        titleInput.addEventListener('blur', () => this.validateTitle());
        titleInput.addEventListener('input', () => this.clearValidationError('titleError'));
        
        descInput.addEventListener('input', (e) => {
            this.updateCharCount(e.target);
            this.validateDescription();
        });
    }

    /**
     * Handle form submission for creating/updating tasks
     */
    async handleFormSubmit(e) {
        e.preventDefault();
        
        if (!this.validateForm()) {
            return;
        }
        
        const formData = new FormData(e.target);
        const taskData = {
            title: formData.get('title').trim(),
            description: formData.get('description').trim()
        };
        
        this.setLoading(true);
        
        try {
            if (this.editingTaskId) {
                await this.updateTask(this.editingTaskId, taskData);
                this.showToast('Task updated successfully!', 'success');
                this.cancelEdit();
            } else {
                await this.createTask(taskData);
                this.showToast('Task created successfully!', 'success');
            }
            
            this.resetForm();
            await this.loadTasks();
            await this.updateStatistics();
            
        } catch (error) {
            console.error('Form submission error:', error);
            this.showError('Failed to save task. Please try again.');
            this.showToast('Failed to save task', 'error');
        } finally {
            this.setLoading(false);
        }
    }

    /**
     * Validate the entire form
     */
    validateForm() {
        const titleValid = this.validateTitle();
        const descValid = this.validateDescription();
        return titleValid && descValid;
    }

    /**
     * Validate title field
     */
    validateTitle() {
        const title = document.getElementById('taskTitle').value.trim();
        const errorElement = document.getElementById('titleError');
        
        if (!title) {
            this.showValidationError(errorElement, 'Title is required');
            return false;
        }
        
        if (title.length > 100) {
            this.showValidationError(errorElement, 'Title must be less than 100 characters');
            return false;
        }
        
        this.clearValidationError('titleError');
        return true;
    }

    /**
     * Validate description field
     */
    validateDescription() {
        const description = document.getElementById('taskDescription').value.trim();
        const errorElement = document.querySelector('#taskDescription + .error-message');
        
        if (description.length > 500) {
            if (errorElement) {
                this.showValidationError(errorElement, 'Description must be less than 500 characters');
            }
            return false;
        }
        
        if (errorElement) {
            this.clearValidationError(errorElement.id);
        }
        return true;
    }

    /**
     * Show validation error
     */
    showValidationError(element, message) {
        element.textContent = message;
        element.classList.add('show');
    }

    /**
     * Clear validation error
     */
    clearValidationError(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = '';
            element.classList.remove('show');
        }
    }

    /**
     * Update character count for description
     */
    updateCharCount(textarea) {
        const charCount = document.getElementById('descCharCount');
        const length = textarea.value.length;
        charCount.textContent = `${length}/500`;
        
        if (length > 450) {
            charCount.style.color = 'var(--error-color)';
        } else if (length > 400) {
            charCount.style.color = 'var(--warning-color)';
        } else {
            charCount.style.color = 'var(--gray-500)';
        }
    }

    /**
     * Handle search functionality with debouncing
     */
    handleSearch(query) {
        clearTimeout(this.debounceTimer);
        this.debounceTimer = setTimeout(() => {
            this.searchTasks(query.trim());
        }, 300);
    }

    /**
     * Search tasks by title
     */
    async searchTasks(query) {
        if (!query) {
            await this.loadTasks();
            return;
        }
        
        this.setLoading(true);
        try {
            const response = await fetch(`${this.apiBaseUrl}/tasks/search?title=${encodeURIComponent(query)}`);
            if (!response.ok) throw new Error('Search failed');
            
            const tasks = await response.json();
            this.tasks = tasks;
            this.renderTasks();
        } catch (error) {
            console.error('Search error:', error);
            this.showError('Search failed. Please try again.');
        } finally {
            this.setLoading(false);
        }
    }

    /**
     * Handle filter changes
     */
    handleFilter(filter) {
        this.currentFilter = filter;
        
        // Update active filter button
        document.querySelectorAll('.btn-filter').forEach(btn => {
            btn.classList.remove('active');
        });
        document.querySelector(`[data-filter="${filter}"]`).classList.add('active');
        
        this.renderTasks();
    }

    /**
     * Load all tasks from the API
     */
    async loadTasks() {
        this.setLoading(true);
        try {
            let endpoint = '/tasks';
            
            if (this.currentFilter === 'completed') {
                endpoint = '/tasks/completed';
            } else if (this.currentFilter === 'incomplete') {
                endpoint = '/tasks/incomplete';
            }
            
            const response = await fetch(`${this.apiBaseUrl}${endpoint}`);
            if (!response.ok) throw new Error('Failed to load tasks');
            
            this.tasks = await response.json();
            this.renderTasks();
            this.hideError();
        } catch (error) {
            console.error('Load tasks error:', error);
            this.showError('Failed to load tasks. Please refresh the page.');
            this.showToast('Failed to load tasks', 'error');
        } finally {
            this.setLoading(false);
        }
    }

    /**
     * Create a new task
     */
    async createTask(taskData) {
        const response = await fetch(`${this.apiBaseUrl}/tasks`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(taskData),
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to create task');
        }
        
        return response.json();
    }

    /**
     * Update an existing task
     */
    async updateTask(id, taskData) {
        const response = await fetch(`${this.apiBaseUrl}/tasks/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(taskData),
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to update task');
        }
        
        return response.json();
    }

    /**
     * Toggle task completion status
     */
    async toggleTaskCompletion(id, completed) {
        try {
            const endpoint = completed ? 'complete' : 'incomplete';
            const response = await fetch(`${this.apiBaseUrl}/tasks/${id}/${endpoint}`, {
                method: 'PUT',
            });
            
            if (!response.ok) throw new Error('Failed to toggle task status');
            
            await this.loadTasks();
            await this.updateStatistics();
            
            const message = completed ? 'Task completed!' : 'Task marked as incomplete!';
            this.showToast(message, 'success');
        } catch (error) {
            console.error('Toggle task error:', error);
            this.showError('Failed to update task status.');
            this.showToast('Failed to update task status', 'error');
        }
    }

    /**
     * Delete a task
     */
    async deleteTask(id) {
        if (!confirm('Are you sure you want to delete this task? This action cannot be undone.')) {
            return;
        }
        
        try {
            const response = await fetch(`${this.apiBaseUrl}/tasks/${id}`, {
                method: 'DELETE',
            });
            
            if (!response.ok) throw new Error('Failed to delete task');
            
            await this.loadTasks();
            await this.updateStatistics();
            this.showToast('Task deleted successfully!', 'success');
        } catch (error) {
            console.error('Delete task error:', error);
            this.showError('Failed to delete task.');
            this.showToast('Failed to delete task', 'error');
        }
    }

    /**
     * Edit a task
     */
    editTask(task) {
        this.editingTaskId = task.id;
        
        // Populate form with task data
        document.getElementById('taskTitle').value = task.title;
        document.getElementById('taskDescription').value = task.description || '';
        
        // Update character count
        this.updateCharCount(document.getElementById('taskDescription'));
        
        // Update form UI
        const submitBtn = document.querySelector('#taskForm button[type="submit"]');
        submitBtn.innerHTML = '<i class="fas fa-save"></i> Update Task';
        
        const cancelBtn = document.getElementById('cancelEdit');
        cancelBtn.style.display = 'inline-flex';
        
        // Scroll to form
        document.querySelector('.task-form-container').scrollIntoView({ behavior: 'smooth' });
        document.getElementById('taskTitle').focus();
    }

    /**
     * Cancel editing mode
     */
    cancelEdit() {
        this.editingTaskId = null;
        this.resetForm();
        
        const submitBtn = document.querySelector('#taskForm button[type="submit"]');
        submitBtn.innerHTML = '<i class="fas fa-plus"></i> Add Task';
        
        const cancelBtn = document.getElementById('cancelEdit');
        cancelBtn.style.display = 'none';
    }

    /**
     * Reset the form
     */
    resetForm() {
        document.getElementById('taskForm').reset();
        document.getElementById('descCharCount').textContent = '0/500';
        document.getElementById('descCharCount').style.color = 'var(--gray-500)';
        this.clearValidationError('titleError');
    }

    /**
     * Render tasks in the UI
     */
    renderTasks() {
        const tasksList = document.getElementById('tasksList');
        const emptyState = document.getElementById('emptyState');
        
        let filteredTasks = this.tasks;
        
        // Apply current filter if showing all tasks
        if (this.currentFilter === 'completed' && this.tasks.length > 0) {
            filteredTasks = this.tasks.filter(task => task.completed);
        } else if (this.currentFilter === 'incomplete' && this.tasks.length > 0) {
            filteredTasks = this.tasks.filter(task => !task.completed);
        }
        
        if (filteredTasks.length === 0) {
            tasksList.innerHTML = '';
            emptyState.style.display = 'block';
            return;
        }
        
        emptyState.style.display = 'none';
        tasksList.innerHTML = filteredTasks.map(task => this.renderTaskItem(task)).join('');
        
        // Attach event listeners to task items
        this.attachTaskEventListeners();
    }

    /**
     * Render a single task item
     */
    renderTaskItem(task) {
        const formattedDate = this.formatDate(task.createdAt);
        const statusText = task.completed ? 'Completed' : 'Pending';
        const statusClass = task.completed ? 'completed' : 'pending';
        const taskClass = task.completed ? 'completed' : '';
        
        return `
            <div class="task-item ${taskClass}" data-task-id="${task.id}">
                <div class="task-content">
                    <div class="task-checkbox-container">
                        <input type="checkbox" class="task-checkbox" ${task.completed ? 'checked' : ''} 
                               data-task-id="${task.id}" />
                        <span class="checkmark"></span>
                    </div>
                    <div class="task-details">
                        <h3 class="task-title">${this.escapeHtml(task.title)}</h3>
                        ${task.description ? `<p class="task-description">${this.escapeHtml(task.description)}</p>` : ''}
                        <div class="task-meta">
                            <span class="task-date">
                                <i class="fas fa-calendar-alt"></i>
                                ${formattedDate}
                            </span>
                            <span class="task-status ${statusClass}">${statusText}</span>
                        </div>
                    </div>
                </div>
                <div class="task-actions">
                    <button class="btn btn-icon btn-edit" title="Edit task" data-task-id="${task.id}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-icon btn-delete" title="Delete task" data-task-id="${task.id}">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    }

    /**
     * Attach event listeners to task items
     */
    attachTaskEventListeners() {
        // Checkbox toggles
        document.querySelectorAll('.task-checkbox').forEach(checkbox => {
            checkbox.addEventListener('change', (e) => {
                const taskId = parseInt(e.target.dataset.taskId);
                this.toggleTaskCompletion(taskId, e.target.checked);
            });
        });
        
        // Edit buttons
        document.querySelectorAll('.btn-edit').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const taskId = parseInt(e.target.closest('.btn-edit').dataset.taskId);
                const task = this.tasks.find(t => t.id === taskId);
                if (task) this.editTask(task);
            });
        });
        
        // Delete buttons
        document.querySelectorAll('.btn-delete').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const taskId = parseInt(e.target.closest('.btn-delete').dataset.taskId);
                this.deleteTask(taskId);
            });
        });
    }

    /**
     * Update header statistics
     */
    async updateStatistics() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/tasks/statistics`);
            if (!response.ok) throw new Error('Failed to load statistics');
            
            const stats = await response.json();
            
            document.getElementById('totalTasks').textContent = stats.totalTasks;
            document.getElementById('completedTasks').textContent = stats.completedTasks;
            document.getElementById('remainingTasks').textContent = stats.incompleteTasks;
        } catch (error) {
            console.error('Statistics error:', error);
        }
    }

    /**
     * Show loading state
     */
    setLoading(isLoading) {
        const spinner = document.getElementById('loadingSpinner');
        const tasksList = document.getElementById('tasksList');
        
        if (isLoading) {
            spinner.style.display = 'block';
            tasksList.style.opacity = '0.5';
        } else {
            spinner.style.display = 'none';
            tasksList.style.opacity = '1';
        }
    }

    /**
     * Show error message
     */
    showError(message) {
        const errorBanner = document.getElementById('errorMessage');
        const errorText = document.getElementById('errorText');
        
        errorText.textContent = message;
        errorBanner.style.display = 'flex';
    }

    /**
     * Hide error message
     */
    hideError() {
        document.getElementById('errorMessage').style.display = 'none';
    }

    /**
     * Show toast notification
     */
    showToast(message, type = 'info') {
        const toastContainer = document.getElementById('toastContainer');
        const toastId = `toast-${Date.now()}`;
        
        const toast = document.createElement('div');
        toast.id = toastId;
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <i class="fas fa-${this.getToastIcon(type)}"></i>
            <span>${this.escapeHtml(message)}</span>
        `;
        
        toastContainer.appendChild(toast);
        
        // Trigger animation
        setTimeout(() => toast.classList.add('show'), 100);
        
        // Auto remove after 4 seconds
        setTimeout(() => this.removeToast(toastId), 4000);
    }

    /**
     * Get toast icon based on type
     */
    getToastIcon(type) {
        const icons = {
            success: 'check-circle',
            error: 'exclamation-circle',
            info: 'info-circle',
            warning: 'exclamation-triangle'
        };
        return icons[type] || icons.info;
    }

    /**
     * Remove toast notification
     */
    removeToast(toastId) {
        const toast = document.getElementById(toastId);
        if (toast) {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }
    }

    /**
     * Format date for display
     */
    formatDate(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diffTime = Math.abs(now - date);
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
        
        if (diffDays === 0) {
            return 'Today';
        } else if (diffDays === 1) {
            return 'Yesterday';
        } else if (diffDays < 7) {
            return `${diffDays} days ago`;
        } else {
            return date.toLocaleDateString();
        }
    }

    /**
     * Escape HTML to prevent XSS
     */
    escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new TaskManager();
});

// Export for testing
if (typeof module !== 'undefined' && module.exports) {
    module.exports = TaskManager;
}