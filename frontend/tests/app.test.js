/**
 * Frontend tests for Task Manager V2
 */

// Mock fetch globally
global.fetch = jest.fn()

describe('TaskManager', () => {
  let taskManager
  let mockConsole

  beforeEach(() => {
    // Reset DOM
    document.body.innerHTML = `
      <div class="app">
        <header class="header">
          <div class="header-stats">
            <span id="totalTasks">0</span>
            <span id="completedTasks">0</span>
            <span id="remainingTasks">0</span>
          </div>
        </header>
        <main>
          <div class="task-form-container">
            <form id="taskForm">
              <input id="taskTitle" name="title" />
              <textarea id="taskDescription" name="description"></textarea>
              <button type="submit">Submit</button>
            </form>
            <button id="cancelEdit" style="display: none;">Cancel</button>
          </div>
          <input id="searchInput" />
          <div id="tasksList"></div>
          <div id="emptyState" style="display: none;"></div>
          <div id="loadingSpinner" style="display: none;"></div>
          <div id="errorMessage" style="display: none;">
            <span id="errorText"></span>
          </div>
          <button id="closeError">Close</button>
          <div id="titleError"></div>
          <div id="descCharCount">0/500</div>
          <div id="toastContainer"></div>
        </main>
        <div class="btn-filter active" data-filter="all">All</div>
        <div class="btn-filter" data-filter="completed">Completed</div>
        <div class="btn-filter" data-filter="incomplete">Incomplete</div>
      </div>
    `

    // Mock console to suppress logs during tests
    mockConsole = {
      log: jest.spyOn(console, 'log').mockImplementation(),
      error: jest.spyOn(console, 'error').mockImplementation(),
      warn: jest.spyOn(console, 'warn').mockImplementation()
    }

    // Reset fetch mock and setup default responses
    fetch.mockClear()
    fetch.mockImplementation((url) => {
      if (url.includes('/tasks/statistics')) {
        return Promise.resolve({
          ok: true,
          json: async () => ({
            totalTasks: 0,
            completedTasks: 0,
            incompleteTasks: 0
          })
        })
      } else if (url.includes('/tasks')) {
        return Promise.resolve({
          ok: true,
          json: async () => []
        })
      }
      return Promise.resolve({
        ok: true,
        json: async () => ({})
      })
    })

    // Create TaskManager instance
    taskManager = new TaskManager()
  })

  afterEach(() => {
    // Restore console
    Object.values(mockConsole).forEach(mock => mock.mockRestore())
  })

  describe('Initialization', () => {
    test('should initialize with default values', () => {
      expect(taskManager.apiBaseUrl).toBe('http://localhost:8080/api')
      expect(taskManager.tasks).toEqual([])
      expect(taskManager.currentFilter).toBe('all')
      expect(taskManager.editingTaskId).toBeNull()
    })

    test('should setup event listeners on init', () => {
      const form = document.getElementById('taskForm')
      const searchInput = document.getElementById('searchInput')
      
      expect(form).toBeDefined()
      expect(searchInput).toBeDefined()
    })
  })

  describe('Form Validation', () => {
    test('should validate title field correctly', () => {
      const titleInput = document.getElementById('taskTitle')
      
      // Test empty title
      titleInput.value = ''
      expect(taskManager.validateTitle()).toBe(false)
      
      // Test valid title
      titleInput.value = 'Valid Task Title'
      expect(taskManager.validateTitle()).toBe(true)
      
      // Test title too long
      titleInput.value = 'a'.repeat(101)
      expect(taskManager.validateTitle()).toBe(false)
    })

    test('should validate description field correctly', () => {
      const descInput = document.getElementById('taskDescription')
      
      // Test valid description
      descInput.value = 'Valid description'
      expect(taskManager.validateDescription()).toBe(true)
      
      // Test description too long
      descInput.value = 'a'.repeat(501)
      expect(taskManager.validateDescription()).toBe(false)
    })

    test('should update character count', () => {
      const descInput = document.getElementById('taskDescription')
      const charCount = document.getElementById('descCharCount')
      
      descInput.value = 'Test description'
      taskManager.updateCharCount(descInput)
      
      expect(charCount.textContent).toBe('16/500')
    })
  })

  describe('API Operations', () => {
    test('should create task successfully', async () => {
      const mockTask = {
        id: 1,
        title: 'New Task',
        description: 'New Description',
        completed: false
      }

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTask
      })

      const result = await taskManager.createTask({
        title: 'New Task',
        description: 'New Description'
      })

      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/tasks',
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            title: 'New Task',
            description: 'New Description'
          })
        })
      )
      expect(result).toEqual(mockTask)
    })

    test('should handle API errors', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'))

      await expect(taskManager.createTask({
        title: 'Test Task',
        description: 'Test Description'
      })).rejects.toThrow('Network error')
    })

    test('should load tasks successfully', async () => {
      const mockTasks = [
        { id: 1, title: 'Task 1', completed: false },
        { id: 2, title: 'Task 2', completed: true }
      ]

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTasks
      })

      await taskManager.loadTasks()

      expect(taskManager.tasks).toEqual(mockTasks)
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/tasks')
    })

    test('should update task successfully', async () => {
      const mockUpdatedTask = {
        id: 1,
        title: 'Updated Task',
        description: 'Updated Description',
        completed: true
      }

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockUpdatedTask
      })

      const result = await taskManager.updateTask(1, {
        title: 'Updated Task',
        description: 'Updated Description',
        completed: true
      })

      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/tasks/1',
        expect.objectContaining({
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(result).toEqual(mockUpdatedTask)
    })
  })

  describe('Task Rendering', () => {
    test('should render tasks correctly', () => {
      const mockTasks = [
        {
          id: 1,
          title: 'Test Task',
          description: 'Test Description',
          completed: false,
          createdAt: new Date().toISOString()
        }
      ]

      taskManager.tasks = mockTasks
      taskManager.renderTasks()

      const tasksList = document.getElementById('tasksList')
      expect(tasksList.innerHTML).toContain('Test Task')
      expect(tasksList.innerHTML).toContain('Test Description')
    })

    test('should show empty state when no tasks', () => {
      taskManager.tasks = []
      taskManager.renderTasks()

      const emptyState = document.getElementById('emptyState')
      expect(emptyState.style.display).toBe('block')
    })

    test('should filter tasks correctly', () => {
      const mockTasks = [
        { id: 1, title: 'Task 1', completed: false },
        { id: 2, title: 'Task 2', completed: true }
      ]

      taskManager.tasks = mockTasks
      taskManager.currentFilter = 'completed'
      taskManager.renderTasks()

      const tasksList = document.getElementById('tasksList')
      expect(tasksList.innerHTML).toContain('Task 2')
      expect(tasksList.innerHTML).not.toContain('Task 1')
    })
  })

  describe('Search Functionality', () => {
    test('should search tasks with debouncing', (done) => {
      // Clear previous fetch calls from initialization
      fetch.mockClear()
      
      fetch.mockResolvedValue({
        ok: true,
        json: async () => []
      })

      taskManager.handleSearch('test query')

      // Should not call immediately
      expect(fetch).not.toHaveBeenCalled()

      // Should call after debounce delay
      setTimeout(() => {
        expect(fetch).toHaveBeenCalledWith(
          'http://localhost:8080/api/tasks/search?title=test%20query'
        )
        done()
      }, 350)
    })
  })

  describe('Utility Functions', () => {
    test('should format dates correctly', () => {
      const today = new Date()
      const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
      const threeDaysAgo = new Date(today.getTime() - 3 * 24 * 60 * 60 * 1000)
      const twoWeeksAgo = new Date(today.getTime() - 14 * 24 * 60 * 60 * 1000)

      expect(taskManager.formatDate(today.toISOString())).toBe('Today')
      expect(taskManager.formatDate(yesterday.toISOString())).toBe('Yesterday')
      expect(taskManager.formatDate(threeDaysAgo.toISOString())).toBe('3 days ago')
      expect(taskManager.formatDate(twoWeeksAgo.toISOString())).toBe(twoWeeksAgo.toLocaleDateString())
    })

    test('should escape HTML correctly', () => {
      const unsafeString = '<script>alert("xss")</script>'
      const escaped = taskManager.escapeHtml(unsafeString)
      
      expect(escaped).toBe('&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;')
      expect(escaped).not.toContain('<script>')
    })

    test('should show and hide loading state', () => {
      const spinner = document.getElementById('loadingSpinner')
      const tasksList = document.getElementById('tasksList')

      taskManager.setLoading(true)
      expect(spinner.style.display).toBe('block')
      expect(tasksList.style.opacity).toBe('0.5')

      taskManager.setLoading(false)
      expect(spinner.style.display).toBe('none')
      expect(tasksList.style.opacity).toBe('1')
    })
  })

  describe('Error Handling', () => {
    test('should show error messages', () => {
      const errorMessage = 'Test error message'
      taskManager.showError(errorMessage)

      const errorBanner = document.getElementById('errorMessage')
      const errorText = document.getElementById('errorText')

      expect(errorBanner.style.display).toBe('flex')
      expect(errorText.textContent).toBe(errorMessage)
    })

    test('should hide error messages', () => {
      taskManager.hideError()

      const errorBanner = document.getElementById('errorMessage')
      expect(errorBanner.style.display).toBe('none')
    })
  })

  describe('Toast Notifications', () => {
    test('should show toast notifications', () => {
      // Clear any existing toasts from initialization
      const toastContainer = document.getElementById('toastContainer')
      toastContainer.innerHTML = ''
      
      taskManager.showToast('Test message', 'success')

      expect(toastContainer.children.length).toBe(1)
      expect(toastContainer.innerHTML).toContain('Test message')
    })

    test('should auto-remove toast notifications', (done) => {
      // Clear any existing toasts from initialization
      const toastContainer = document.getElementById('toastContainer')
      toastContainer.innerHTML = ''
      
      taskManager.showToast('Test message', 'info')

      expect(toastContainer.children.length).toBe(1)

      // Wait for toast to be removed (4000ms + 300ms animation)
      setTimeout(() => {
        expect(toastContainer.children.length).toBe(0)
        done()
      }, 4500)
    })
  })

  describe('Edit Mode', () => {
    test('should enter edit mode correctly', () => {
      const task = {
        id: 1,
        title: 'Edit Test Task',
        description: 'Edit test description'
      }

      taskManager.editTask(task)

      expect(taskManager.editingTaskId).toBe(1)
      expect(document.getElementById('taskTitle').value).toBe('Edit Test Task')
      expect(document.getElementById('taskDescription').value).toBe('Edit test description')
      expect(document.getElementById('cancelEdit').style.display).toBe('inline-flex')
    })

    test('should cancel edit mode correctly', () => {
      taskManager.editingTaskId = 1
      taskManager.cancelEdit()

      expect(taskManager.editingTaskId).toBeNull()
      expect(document.getElementById('cancelEdit').style.display).toBe('none')
    })
  })
})