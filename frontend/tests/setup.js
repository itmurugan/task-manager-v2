/**
 * Test setup file for Task Manager Frontend
 */

// Global test utilities
const TaskManager = require('../app.js')
global.TaskManager = TaskManager

// Mock browser APIs that might not be available in Jest
Object.defineProperty(window, 'scrollTo', {
  value: jest.fn(),
  writable: true
})

Object.defineProperty(Element.prototype, 'scrollIntoView', {
  value: jest.fn(),
  writable: true
})

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn()
}
global.localStorage = localStorageMock

// Mock sessionStorage
global.sessionStorage = localStorageMock

// Mock window.confirm
global.confirm = jest.fn(() => true)

// Mock window.alert
global.alert = jest.fn()

// Set up global test timeout
jest.setTimeout(10000)

// Clean up after each test
afterEach(() => {
  jest.clearAllMocks()
  document.body.innerHTML = ''
})