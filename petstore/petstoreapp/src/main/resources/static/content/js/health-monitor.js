/**
 * Enhanced Health Monitor for Pet Store Application
 * Shows detailed service status information
 */
class HealthMonitor {
    constructor(options = {}) {
        this.options = {
            checkInterval: options.checkInterval || 30000, // 30 seconds
            retryDelay: options.retryDelay || 5000, // 5 seconds on error
            maxRetries: options.maxRetries || 3,
            timeout: options.timeout || 5000, // 5 second timeout
            ...options
        };

        this.statusElement = document.getElementById('health-status-badge');
        this.retryCount = 0;
        this.lastStatus = null;
        this.intervalId = null;

        this.init();
    }

    async init() {
        console.log('Health Monitor initialized');
        await this.checkHealth();
        this.startMonitoring();
        this.addClickHandler();
    }

    startMonitoring() {
        this.intervalId = setInterval(() => {
            this.checkHealth();
        }, this.options.checkInterval);

        console.log(`Health monitoring started (interval: ${this.options.checkInterval}ms)`);
    }

    async checkHealth() {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), this.options.timeout);

            const response = await fetch('/actuator/health', {
                signal: controller.signal,
                cache: 'no-cache',
                headers: {
                    'Accept': 'application/json'
                }
            });

            clearTimeout(timeoutId);

            // Accept both 200 (UP) and 503 (DOWN) as valid responses
            if (response.ok || response.status === 503) {
                const data = await response.json();
                this.handleHealthResponse(data);
                this.retryCount = 0; // Reset retry count on success
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

        } catch (error) {
            this.handleHealthError(error);
        }
    }

    handleHealthResponse(data) {
        const overallStatus = data.status;
        const components = data.components || {};

        // Analyze component statuses
        const componentStatuses = Object.keys(components)
            .filter(key => key !== 'refreshScope')
            .map(key => ({
                name: key,
                status: components[key].status,
                details: components[key].details
            }));

        const upServices = componentStatuses.filter(c => c.status === 'UP');
        const downServices = componentStatuses.filter(c => c.status === 'DOWN');
        const totalServices = componentStatuses.length;

        // Create detailed status message
        let statusMessage;
        let statusClass;
        let iconClass;

        if (overallStatus === 'UP' && downServices.length === 0) {
            statusMessage = `All ${totalServices} Services UP`;
            statusClass = 'up';
            iconClass = 'fa-check-circle';
        } else if (downServices.length > 0) {
            // Show which services are down
            const downServiceNames = downServices.map(s => this.getServiceDisplayName(s.name));
            if (downServices.length === 1) {
                statusMessage = `${downServiceNames[0]} DOWN`;
            } else {
                statusMessage = `${downServices.length} of ${totalServices} Services DOWN`;
            }
            statusClass = 'down';
            iconClass = 'fa-exclamation-triangle';
        } else {
            statusMessage = `${upServices.length}/${totalServices} Services UP`;
            statusClass = 'warning';
            iconClass = 'fa-exclamation-triangle';
        }

        // Update status with detailed tooltip
        this.updateStatus(statusClass, statusMessage, iconClass);
        this.updateSimpleTooltip(componentStatuses);

        // Log detailed status for debugging
        console.log('Health check result:', {
            overall: overallStatus,
            upServices: upServices.length,
            downServices: downServices.length,
            downServiceNames: downServices.map(s => s.name),
            components: componentStatuses
        });

        this.lastStatus = overallStatus;
    }

    handleHealthError(error) {
        console.warn('Health check failed:', error.message);

        if (error.name === 'AbortError') {
            console.warn('Health check timed out');
        }

        if (this.retryCount < this.options.maxRetries) {
            this.retryCount++;
            this.updateStatus('checking', `Retrying... (${this.retryCount}/${this.options.maxRetries})`, 'fa-sync fa-spin');

            // Retry after delay
            setTimeout(() => {
                this.checkHealth();
            }, this.options.retryDelay);
        } else {
            this.updateStatus('down', 'Health Check Failed', 'fa-times-circle');
            this.updateSimpleTooltip([]);
        }
    }

    updateStatus(status, text, iconClass) {
        if (!this.statusElement) return;

        // Add transition for smooth changes
        this.statusElement.style.transition = 'all 0.3s ease';
        this.statusElement.className = `health-status-badge status-${status}`;
        this.statusElement.innerHTML = `<i class="fas ${iconClass}"></i><span>${text}</span>`;

        console.log(`Health status updated: ${status} - ${text}`);
    }

    updateSimpleTooltip(components) {
        if (!this.statusElement) return;

        let tooltipText = `Last checked: ${new Date().toLocaleTimeString()} | Click to refresh\n\n`;

        if (components.length > 0) {
            tooltipText += 'Services:\n';
            components.forEach(component => {
                const displayName = this.getServiceDisplayName(component.name);
                const status = component.status === 'UP' ? 'UP' : 'DOWN';
                const icon = component.status === 'UP' ? '✅' : '❌';
                tooltipText += `${icon} ${displayName}: ${status}`;

                // Add error for DOWN services
                if (component.status === 'DOWN' && component.details && component.details.error) {
                    const error = component.details.error;
                    if (error.includes('Connection refused')) {
                        tooltipText += ` (Service offline)\n`;
                    } else if (error.length > 40) {
                        tooltipText += `\n    (${error.substring(0, 40)}...)\n`;
                    } else {
                        tooltipText += `\n    (${error})\n`;
                    }
                } else {
                    tooltipText += '\n';
                }
            });
        } else {
            tooltipText += 'Unable to retrieve service status';
        }

        // Only set browser title tooltip
        this.statusElement.title = tooltipText;
    }

    getServiceDisplayName(serviceName) {
        const displayNames = {
            'petService': 'Pet Service',
            'productService': 'Product Service',
            'orderService': 'Order Service',
            'applicationInsights': 'App Insights',
            'security': 'Security',
            'ping': 'System Health'
        };
        return displayNames[serviceName] || serviceName;
    }

    addClickHandler() {
        if (this.statusElement) {
            this.statusElement.style.cursor = 'pointer';
            this.statusElement.title = 'Click to refresh health status';

            this.statusElement.addEventListener('click', () => {
                this.refresh();
            });
        }
    }

    // Manual refresh method
    async refresh() {
        console.log('Manual health check requested');
        this.updateStatus('checking', 'Refreshing...', 'fa-sync fa-spin');
        this.retryCount = 0; // Reset retry count for manual refresh
        await this.checkHealth();
    }

    // Stop monitoring
    stop() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
            console.log('Health monitoring stopped');
        }
    }

    // Restart monitoring
    restart() {
        this.stop();
        this.startMonitoring();
        console.log('Health monitoring restarted');
    }

    // Get current status for external access
    getCurrentStatus() {
        return {
            element: this.statusElement,
            lastCheck: new Date().toISOString(),
            retryCount: this.retryCount
        };
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // Create global health monitor instance
    window.healthMonitor = new HealthMonitor({
        checkInterval: 30000, // Check every 30 seconds
        maxRetries: 3,
        timeout: 5000
    });

    // Add visibility change handler to pause monitoring when tab is not visible
    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'visible') {
            // Tab became visible - restart monitoring and check immediately
            if (window.healthMonitor) {
                window.healthMonitor.restart();
            }
        } else {
            // Tab became hidden - stop monitoring to save resources
            if (window.healthMonitor) {
                window.healthMonitor.stop();
            }
        }
    });
});

// Expose helper functions for console debugging
window.debugHealth = {
    check: () => window.healthMonitor?.refresh(),
    stop: () => window.healthMonitor?.stop(),
    start: () => window.healthMonitor?.restart(),
    status: () => console.log('Health monitor:', window.healthMonitor?.getCurrentStatus()),
    current: () => window.healthMonitor?.getCurrentStatus()
};