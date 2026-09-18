/* Smart Online Examination Engine & Security Proctoring */
let secondsRemaining = 0;
let timerInterval = null;
let currentAttemptId = null;
let currentQuestionIndex = 0;
let totalQuestionsCount = 0;

function initExamEngine(attemptId, initialSeconds) {
    currentAttemptId = attemptId;
    secondsRemaining = initialSeconds;
    
    // Start countdown timer
    startCountdown();

    // Enable security proctoring
    enableSecurityProctoring();

    // Sync palette indicators
    updatePaletteState();
}

function startCountdown() {
    const timerDisplay = document.getElementById('timerDisplay');
    const timerBox = document.getElementById('timerBox');

    updateTimerUI();

    timerInterval = setInterval(() => {
        secondsRemaining--;

        if (secondsRemaining <= 180 && timerBox) {
            timerBox.classList.add('timer-warning');
        }

        if (secondsRemaining <= 0) {
            clearInterval(timerInterval);
            autoSubmitExam('AUTO_SUBMIT_EXPIRED');
        } else {
            updateTimerUI();
        }
    }, 1000);
}

function updateTimerUI() {
    const timerDisplay = document.getElementById('timerDisplay');
    if (!timerDisplay) return;

    const mins = Math.floor(secondsRemaining / 60);
    const secs = secondsRemaining % 60;
    timerDisplay.textContent = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

function saveAnswer(questionId, isMarkedForReview = false) {
    if (!currentAttemptId) return;

    let selectedOptionIds = '';
    let textAnswer = '';

    // Collect radio options
    const radios = document.querySelectorAll(`input[name="q_${questionId}"]:checked`);
    if (radios.length > 0) {
        selectedOptionIds = Array.from(radios).map(r => r.value).join(',');
    }

    // Collect checkbox options (MCQ Multiple)
    const checkboxes = document.querySelectorAll(`input[name="q_multi_${questionId}"]:checked`);
    if (checkboxes.length > 0) {
        selectedOptionIds = Array.from(checkboxes).map(c => c.value).join(',');
    }

    // Collect text input (Fill in blank)
    const textInput = document.getElementById(`q_text_${questionId}`);
    if (textInput) {
        textAnswer = textInput.value;
    }

    const payload = {
        studentExamId: currentAttemptId,
        questionId: questionId,
        selectedOptionIds: selectedOptionIds,
        textAnswer: textAnswer,
        markedForReview: isMarkedForReview
    };

    // Update UI palette button immediately
    const paletteBtn = document.getElementById(`palette_btn_${questionId}`);
    if (paletteBtn) {
        if (isMarkedForReview) {
            paletteBtn.className = 'palette-btn marked';
        } else if ((selectedOptionIds && selectedOptionIds.trim().length > 0) || (textAnswer && textAnswer.trim().length > 0)) {
            paletteBtn.className = 'palette-btn attempted';
        } else {
            paletteBtn.className = 'palette-btn';
        }
    }

    // AJAX call to backend
    fetch('/api/exam/save-answer', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        const statusBadge = document.getElementById('autoSaveBadge');
        if (statusBadge) {
            statusBadge.style.display = 'inline-flex';
            setTimeout(() => { statusBadge.style.display = 'none'; }, 1500);
        }
    })
    .catch(err => console.error('Error auto-saving answer:', err));
}

function autoSubmitExam(source) {
    if (timerInterval) clearInterval(timerInterval);

    // Remove beforeunload warning
    window.onbeforeunload = null;

    const form = document.getElementById('examSubmitForm');
    if (form) {
        document.getElementById('submissionSourceInput').value = source;
        form.submit();
    }
}

function enableSecurityProctoring() {
    // Disable right click
    document.addEventListener('contextmenu', e => e.preventDefault());

    // Disable copy/cut/paste
    document.addEventListener('copy', e => e.preventDefault());
    document.addEventListener('cut', e => e.preventDefault());
    document.addEventListener('paste', e => e.preventDefault());

    // Disable keyboard shortcuts F12, Ctrl+Shift+I, Ctrl+C, Ctrl+V, Alt+Tab
    document.addEventListener('keydown', e => {
        if (e.keyCode === 123 || 
           (e.ctrlKey && e.shiftKey && e.keyCode === 73) || 
           (e.ctrlKey && (e.keyCode === 67 || e.keyCode === 86 || e.keyCode === 85))) {
            e.preventDefault();
            logSecurityViolation('Keyboard shortcut attempt detected.');
        }
    });

    // Detect Tab switching / Focus loss
    window.addEventListener('blur', () => {
        logSecurityViolation('Window lost focus or tab switched.');
    });

    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            logSecurityViolation('Tab switch detected.');
        }
    });

    // Prevent accidental reload
    window.onbeforeunload = function() {
        return "Are you sure you want to leave the exam? Your progress will be saved.";
    };
}

let warningLoggedTime = 0;
function logSecurityViolation(reason) {
    const now = Date.now();
    if (now - warningLoggedTime < 3000) return; // Debounce 3s
    warningLoggedTime = now;

    fetch(`/api/exam/log-warning?attemptId=${currentAttemptId}&reason=${encodeURIComponent(reason)}`, {
        method: 'POST'
    })
    .then(res => res.json())
    .then(data => {
        if (data.disqualified) {
            alert('SECURITY VIOLATION MAXIMUM THRESHOLD EXCEEDED. YOUR EXAM HAS BEEN DISQUALIFIED AND SUBMITTED.');
            autoSubmitExam('DISQUALIFIED');
        } else {
            showSecurityAlert(`SECURITY WARNING #${data.warningCount}/${data.maxWarnings}: ${reason}`);
        }
    })
    .catch(err => console.error(err));
}

function showSecurityAlert(msg) {
    const alertBox = document.createElement('div');
    alertBox.className = 'security-toast';
    alertBox.innerHTML = `<i class="fas fa-exclamation-triangle me-2"></i> ${msg}`;
    document.body.appendChild(alertBox);
    setTimeout(() => alertBox.remove(), 4000);
}

function updatePaletteState() {
    // Synchronize initial attempt state
}
