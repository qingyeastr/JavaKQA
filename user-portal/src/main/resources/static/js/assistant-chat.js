(function () {
	const root = document.querySelector('[data-assistant-app]');
	if (!root) {
		return;
	}

	const state = {
		conversations: [],
		currentConversationId: null,
		currentMessages: [],
		runtimeConfig: null,
		searchKeyword: '',
		busy: false,
	};

	const urls = {
		runtime: root.dataset.runtimeUrl,
		conversations: root.dataset.conversationsUrl,
		chat: root.dataset.chatUrl,
	};

	const elements = {
		conversationGroups: root.querySelector('[data-conversation-groups]'),
		conversationSearch: root.querySelector('[data-conversation-search]'),
		currentTitle: root.querySelector('[data-current-title]'),
		connectionStatus: root.querySelector('[data-connection-status]'),
		currentStatus: root.querySelector('[data-current-status]'),
		composerStatus: root.querySelector('[data-composer-status]'),
		messageList: root.querySelector('[data-message-list]'),
		form: root.querySelector('[data-chat-form]'),
		input: root.querySelector('[data-chat-input]'),
		sendButton: root.querySelector('[data-send-button]'),
		newConversationButton: root.querySelector('[data-new-conversation]'),
		deleteConversationButton: root.querySelector('[data-delete-conversation]'),
		runtimeModel: root.querySelector('[data-runtime-model]'),
		runtimeRetrieval: root.querySelector('[data-runtime-retrieval]'),
		runtimeReference: root.querySelector('[data-runtime-reference]'),
	};

	bindEvents();
	initialize();

	function bindEvents() {
		elements.form.addEventListener('submit', handleSubmit);
		elements.newConversationButton.addEventListener('click', handleCreateConversation);
		elements.deleteConversationButton.addEventListener('click', handleDeleteConversation);
		elements.conversationSearch.addEventListener('input', (event) => {
			state.searchKeyword = event.target.value.trim().toLowerCase();
			renderConversationGroups();
		});
		elements.conversationGroups.addEventListener('click', async (event) => {
			const target = event.target.closest('[data-conversation-id]');
			if (!target || state.busy) {
				return;
			}
			const conversationId = target.dataset.conversationId;
			if (conversationId) {
				await loadConversation(conversationId);
			}
		});
		elements.messageList.addEventListener('click', (event) => {
			const suggestion = event.target.closest('[data-follow-up]');
			if (!suggestion) {
				return;
			}
			elements.input.value = suggestion.dataset.followUp || '';
			elements.input.focus();
		});
		root.querySelectorAll('[data-sample-question]').forEach((button) => {
			button.addEventListener('click', () => {
				elements.input.value = button.dataset.sampleQuestion || '';
				elements.input.focus();
			});
		});
	}

	async function initialize() {
		setConnectionStatus('正在连接 intelligent-qa', false);
		await loadRuntimeConfig();
		await loadConversations();
	}

	async function loadRuntimeConfig() {
		try {
			const runtimeConfig = await requestJson(urls.runtime);
			state.runtimeConfig = runtimeConfig;
			elements.runtimeModel.textContent = runtimeConfig.model.modelId;
			elements.runtimeRetrieval.textContent = `${runtimeConfig.retrieval.keywordTopK} / ${runtimeConfig.retrieval.semanticTopK} · TopK`;
			elements.runtimeReference.textContent = runtimeConfig.prompt.referenceEnabled ? '展示引用来源' : '仅展示答案';
			setConnectionStatus('intelligent-qa 已连接', false);
		} catch (error) {
			elements.runtimeModel.textContent = '加载失败';
			elements.runtimeRetrieval.textContent = '请检查模块状态';
			elements.runtimeReference.textContent = '暂不可用';
			setConnectionStatus('intelligent-qa 不可用', true);
			setComposerStatus(error.message, true);
		}
	}

	async function loadConversations() {
		try {
			state.conversations = await requestJson(urls.conversations);
			renderConversationGroups();
			if (!state.conversations.length) {
				state.currentConversationId = null;
				state.currentMessages = [];
				setCurrentConversation(null);
				renderMessages([]);
				return;
			}
			const conversationExists = state.currentConversationId
				&& state.conversations.some((conversation) => conversation.conversationId === state.currentConversationId);
			const targetConversationId = conversationExists
				? state.currentConversationId
				: state.conversations[0].conversationId;
			await loadConversation(targetConversationId);
		} catch (error) {
			renderConversationError(error.message);
			setComposerStatus(error.message, true);
		}
	}

	async function loadConversation(conversationId) {
		try {
			const response = await requestJson(`${urls.conversations}/${encodeURIComponent(conversationId)}/messages`);
			state.currentConversationId = conversationId;
			state.currentMessages = Array.isArray(response.messages) ? response.messages : [];
			renderConversationGroups();
			setCurrentConversation({
				conversationId,
				title: response.title,
				status: response.status,
			});
			renderMessages(state.currentMessages);
			scrollMessagesToBottom();
		} catch (error) {
			setComposerStatus(error.message, true);
		}
	}

	async function handleCreateConversation() {
		if (state.busy) {
			return;
		}
		try {
			const response = await requestJson(urls.conversations, { method: 'POST' });
			state.currentConversationId = response.conversationId;
			setComposerStatus('已创建新会话，可以直接开始提问。', false);
			await loadConversations();
		} catch (error) {
			setComposerStatus(error.message, true);
		}
	}

	async function handleDeleteConversation() {
		if (!state.currentConversationId || state.busy) {
			return;
		}
		const targetConversationId = state.currentConversationId;
		try {
			await requestJson(`${urls.conversations}/${encodeURIComponent(targetConversationId)}`, {
				method: 'DELETE',
			});
			state.currentConversationId = null;
			state.currentMessages = [];
			setComposerStatus('当前会话已删除。', false);
			await loadConversations();
		} catch (error) {
			setComposerStatus(error.message, true);
		}
	}

	async function handleSubmit(event) {
		event.preventDefault();
		const message = elements.input.value.trim();
		if (!message || state.busy) {
			return;
		}

		state.busy = true;
		toggleBusy(true);
		setComposerStatus('正在向 intelligent-qa 发送问题...', false);

		try {
			const response = await requestJson(urls.chat, {
				method: 'POST',
				body: JSON.stringify({
					conversationId: state.currentConversationId,
					message,
				}),
			});
			elements.input.value = '';
			state.currentConversationId = response.conversationId;
			setComposerStatus('接口已返回占位答案，可继续追问。', false);
			await loadConversations();
			setCurrentStatus(response.status, false);
		} catch (error) {
			setComposerStatus(error.message, true);
		} finally {
			state.busy = false;
			toggleBusy(false);
		}
	}

	function renderConversationGroups() {
		const filteredConversations = state.conversations.filter((conversation) => {
			if (!state.searchKeyword) {
				return true;
			}
			const combinedText = `${conversation.title} ${conversation.lastPreview || ''}`.toLowerCase();
			return combinedText.includes(state.searchKeyword);
		});

		if (!filteredConversations.length) {
			elements.conversationGroups.innerHTML = '<div class="qa-empty-state">当前没有匹配的会话结果。</div>';
			return;
		}

		const groups = {
			'今天': [],
			'近 7 天': [],
			'更早': [],
		};

		filteredConversations.forEach((conversation) => {
			groups[resolveGroupLabel(conversation.updatedAt)].push(conversation);
		});

		elements.conversationGroups.innerHTML = Object.entries(groups)
			.filter(([, conversations]) => conversations.length)
			.map(([groupLabel, conversations]) => `
				<section class="qa-conversation-group">
					<h4>${groupLabel}</h4>
					<div class="qa-conversation-list">
						${conversations.map((conversation) => renderConversationItem(conversation)).join('')}
					</div>
				</section>
			`)
			.join('');
	}

	function renderConversationItem(conversation) {
		const activeClass = conversation.conversationId === state.currentConversationId ? ' active' : '';
		const updatedText = formatRelativeDate(conversation.updatedAt || conversation.lastMessageAt);
		return `
			<button class="qa-conversation-item${activeClass}" type="button" data-conversation-id="${conversation.conversationId}">
				<strong>${escapeHtml(conversation.title)}</strong>
				<span>${escapeHtml(conversation.lastPreview || '等待你的第一条问题')}</span>
				<small>${escapeHtml(updatedText)}</small>
			</button>
		`;
	}

	function renderMessages(messages) {
		if (!messages.length) {
			elements.messageList.innerHTML = '<div class="qa-empty-state">当前会话还没有消息，直接在底部输入区发出第一条问题。</div>';
			return;
		}

		elements.messageList.innerHTML = messages.map((message) => {
			const isUser = message.role === 'user';
			const citations = !isUser && Array.isArray(message.citations) && message.citations.length
				? `
					<div class="qa-citation-list">
						${message.citations.map((citation, index) => `
							<div class="qa-citation-card">
								<strong>引用 ${String(index + 1).padStart(2, '0')}</strong>
								<span>${escapeHtml(citation.documentTitle || '')}</span>
								<p>${escapeHtml(citation.snippet || '')}</p>
							</div>
						`).join('')}
					</div>
				`
				: '';
			const suggestions = !isUser && Array.isArray(message.followUpSuggestions) && message.followUpSuggestions.length
				? `
					<div class="qa-suggestion-list">
						${message.followUpSuggestions.map((suggestion) => `
							<button class="qa-suggestion-button" type="button" data-follow-up="${escapeHtmlAttribute(suggestion)}">${escapeHtml(suggestion)}</button>
						`).join('')}
					</div>
				`
				: '';
			return `
				<article class="qa-message ${isUser ? 'is-user' : 'is-assistant'}">
					<div class="qa-message-head">
						<strong>${isUser ? '你' : '智能问答助手'}</strong>
						<span>${escapeHtml(formatClock(message.createdAt))}</span>
					</div>
					<div class="qa-message-body">${escapeHtml(message.content || '')}</div>
					${citations}
					${suggestions}
				</article>
			`;
		}).join('');
	}

	function renderConversationError(message) {
		elements.conversationGroups.innerHTML = `<div class="qa-empty-state">${escapeHtml(message)}</div>`;
	}

	function setCurrentConversation(conversation) {
		if (!conversation) {
			elements.currentTitle.textContent = '新对话';
			setCurrentStatus('等待新对话', false);
			elements.deleteConversationButton.disabled = true;
			return;
		}
		elements.currentTitle.textContent = conversation.title || '新对话';
		setCurrentStatus(conversation.status || 'ACTIVE', false);
		elements.deleteConversationButton.disabled = false;
	}

	function setConnectionStatus(message, isError) {
		elements.connectionStatus.textContent = message;
		elements.connectionStatus.classList.toggle('is-error', Boolean(isError));
	}

	function setCurrentStatus(message, isError) {
		elements.currentStatus.textContent = message;
		elements.currentStatus.classList.toggle('is-error', Boolean(isError));
	}

	function setComposerStatus(message, isError) {
		elements.composerStatus.textContent = message;
		elements.composerStatus.classList.toggle('is-error', Boolean(isError));
	}

	function toggleBusy(disabled) {
		elements.sendButton.disabled = disabled;
		elements.newConversationButton.disabled = disabled;
		elements.deleteConversationButton.disabled = disabled || !state.currentConversationId;
	}

	function scrollMessagesToBottom() {
		elements.messageList.scrollTop = elements.messageList.scrollHeight;
	}

	async function requestJson(url, options = {}) {
		const fetchOptions = {
			method: options.method || 'GET',
			headers: {
				'Accept': 'application/json',
			},
		};

		if (options.body) {
			fetchOptions.headers['Content-Type'] = 'application/json';
			fetchOptions.body = options.body;
		}

		const response = await fetch(url, fetchOptions);
		if (response.status === 204) {
			return null;
		}

		const contentType = response.headers.get('content-type') || '';
		if (!contentType.includes('application/json')) {
			throw new Error('返回结果不是 JSON，请确认当前登录态和 intelligent-qa 服务状态。');
		}

		const payload = await response.json();
		if (!response.ok) {
			throw new Error(payload.message || '智能问答接口调用失败');
		}
		return payload;
	}

	function resolveGroupLabel(dateText) {
		if (!dateText) {
			return '更早';
		}
		const date = new Date(dateText);
		const today = new Date();
		const startOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());
		const diffMs = startOfToday.getTime() - new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
		const diffDays = Math.round(diffMs / (24 * 60 * 60 * 1000));
		if (diffDays <= 0) {
			return '今天';
		}
		if (diffDays <= 6) {
			return '近 7 天';
		}
		return '更早';
	}

	function formatRelativeDate(dateText) {
		if (!dateText) {
			return '刚刚';
		}
		const date = new Date(dateText);
		const group = resolveGroupLabel(dateText);
		if (group === '今天') {
			return `今天 ${formatClock(dateText)}`;
		}
		return `${group} · ${new Intl.DateTimeFormat('zh-CN', {
			month: 'numeric',
			day: 'numeric',
			hour: '2-digit',
			minute: '2-digit',
		}).format(date)}`;
	}

	function formatClock(dateText) {
		if (!dateText) {
			return '--:--';
		}
		return new Intl.DateTimeFormat('zh-CN', {
			hour: '2-digit',
			minute: '2-digit',
			hour12: false,
		}).format(new Date(dateText));
	}

	function escapeHtml(value) {
		return String(value)
			.replaceAll('&', '&amp;')
			.replaceAll('<', '&lt;')
			.replaceAll('>', '&gt;')
			.replaceAll('"', '&quot;')
			.replaceAll("'", '&#39;');
	}

	function escapeHtmlAttribute(value) {
		return escapeHtml(value).replaceAll('`', '&#96;');
	}
})();