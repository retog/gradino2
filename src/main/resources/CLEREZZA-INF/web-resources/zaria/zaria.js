/*
    Zaria - a simple web-based rich text editor
    Copyright (c)2010
*/
var Zaria = (function(id, options) {
	var defaults = {
		layout: '<div class="zariaToolbar">[bold][italic][underline]</div>[edit-area]',
		buttons: [
			{name:'bold', label:'Bold', cmd:'bold', className:'bold'},
			{name:'italic', label:'Italic', cmd:'italic', className:'italic'},
			{name:'underline', label:'Underline', cmd:'underline', className:'underline'}
		]
	};
	
	function $(id) {
		return document.getElementById(id);
	}
	
	function build() {
		if(document.designMode) {
			var textarea = $(this.id),
			width = textarea.style.width,
			height = textarea.style.height,
			content = textarea.value,
			className = textarea.className,
			toolbar, i, j, button, buttonClass,
			iframe, wrapperDiv, doc;
			
			this.mode = 'html';
			textarea.style.display = 'none';
			
			toolbar = this.options.layout;
			for(i in this.options.buttons) {
				button = '';
				buttonClass = this.options.buttons[i].className || '';
				if(this.options.buttons[i].menu) {
					button = '<select name="'+this.frameId+'" id="'+this.frameId+'-'+this.options.buttons[i].name+'" class="'+buttonClass+'" ><option>Select '+this.options.buttons[i].label+'</option>';
					for(j in this.options.buttons[i].menu) { button += '<option value="'+this.options.buttons[i].menu[j].value+'">'+this.options.buttons[i].menu[j].label+'</option>'; }
					button += '</select>';
				} else {
					button = '<a name="'+this.frameId+'" id="'+this.frameId+'-'+this.options.buttons[i].name+'" class="'+buttonClass+'" width="20" height="20" alt="'+this.options.buttons[i].label+'" title="'+this.options.buttons[i].label+'" href="javascript:void(0);" ></a>';
				}
				toolbar = toolbar.replace("["+this.options.buttons[i].name+"]", button);
			}
			iframe = '<iframe id="'+this.frameId+'" width="'+width+'" height="'+height+'" style="width:'+width+'; height:'+height+'; border-width: thin;" class="'+className+'" frameborder="1"></iframe>';
			toolbar = toolbar.replace("[edit-area]", iframe);
			
			wrapperDiv = document.createElement('div');
			wrapperDiv.innerHTML = toolbar;
			insertAfter(wrapperDiv, textarea);
			initButtons.call(this, this.frameId);
			
			doc = getIFrameDocument(this.frameId);
			// Write the textarea's content into the iframe
			doc.open();
			doc.write('<html><head></head><body>'+content+'</body></html>');
			doc.close();
			
			// Make the iframe editable
			doc.body.contentEditable = true;
			doc.designMode = 'on';
		}
	}
	
	function getContents() {
		if(document.designMode) {
			// Explorer reformats HTML during document.write() removing quotes on element ID names
			// so we need to address Explorer elements as window[elementID]
			if(window[this.frameId]) {
				return window[this.frameId].document.body.innerHTML;
			}
			return $(this.frameId).contentWindow.document.body.innerHTML;
		} else {
			// return the value from the <textarea> if document.designMode does not exist
			return $(this.id).value;
		}
	}

	function syncContent() {
		var content = '';
		
		if(this.mode == 'text') {
			var iframe = getIFrameDocument(this.frameId);
			if(document.all) {
				var output = escape(iframe.body.innerText);
				output = output.replace("%3CP%3E%0D%0A%3CHR%3E", "%3CHR%3E");
				output = output.replace("%3CHR%3E%0D%0A%3C/P%3E", "%3CHR%3E");
				content = unescape(output);
			} else {
				var htmlSrc = iframe.body.ownerDocument.createRange();
				htmlSrc.selectNodeContents(iframe.body);
				content = htmlSrc.toString();
			}
		} else {
			content = getContents.call(this);
		}
		if(typeof(HTMLtoXML) != 'undefined') {
			content = HTMLtoXML(content);
		}
		$(this.id).value = content;
	}

	function initButtons(id) {
		var self = this, i, currentElement;
		for(i in this.options.buttons) {
			currentElement = $(id+'-'+this.options.buttons[i].name);
			if(currentElement && currentElement.name == id) {
				currentElement.onmouseup = 'return false;';
				if(this.options.buttons[i].menu) {
					currentElement.onchange = (function(element, buttonIndex) { return function() { selectOnChange.call(self, element, self.options.buttons[buttonIndex]); }; })(currentElement,i);
				} else if(this.options.buttons[i].prompt) {
					currentElement.onclick = (function(buttonIndex) { return function() { inputPrompt.call(self, self.options.buttons[buttonIndex]); }; })(i);
				} else if(this.options.buttons[i].toggleMode) {
					currentElement.onclick = (function(element) { return function() { toggleMode.call(self, element); }; })(currentElement);
				} else if(this.options.buttons[i].custom) {
					currentElement.onclick = this.options.buttons[i].custom;
				} else {
					currentElement.onclick = (function(buttonIndex) { return function() { buttonOnClick.call(self, self.options.buttons[buttonIndex]); }; })(i);
				}
			}
		}
	}

	function buttonOnClick(button) {
		// Explorer reformats HTML during document.write() removing quotes on element ID names
		// so we need to address Explorer elements as window[elementID]
		var ea = window[this.frameId] || $(this.frameId).contentWindow;
		ea.focus();
		ea.document.execCommand(button.cmd, false, null);
		ea.focus();
	}

	function selectOnChange(element, button) {
		var cursel = element.selectedIndex;
		if(cursel !== 0) {
			var ea = window[this.frameId] || $(this.frameId).contentWindow;
			ea.focus();
			ea.document.execCommand(button.cmd, false, element.options[cursel].value);
			ea.focus();
			//element.selectedIndex = 0;
		}
	}

	function inputPrompt(button) {
		var value = prompt(button.prompt, "");
		if(value) {
			var ea = window[this.frameId] || $(this.frameId).contentWindow;
			ea.focus();
			ea.document.execCommand(button.cmd, false, value);
			ea.focus();
		}
	}

	function toggleMode(element) {
		var iframe = getIFrameDocument(this.frameId), i;
		if(this.mode == 'html') {
			for(i in this.options.buttons) { currentElement = $(this.frameId+'-'+this.options.buttons[i].name); if(currentElement != element) {currentElement.style.display = 'none';} }
			if(document.all) {
				iframe.body.innerText = iframe.body.innerHTML;
			} else {
				var htmlSrc = iframe.createTextNode(iframe.body.innerHTML);
				iframe.body.innerHTML = "";
				iframe.body.appendChild(htmlSrc);
			}
			this.mode = 'text';
		} else {
			for(i in this.options.buttons) { currentElement = $(this.frameId+'-'+this.options.buttons[i].name); if(currentElement != element) {currentElement.style.display = '';} }
			if(document.all) {
				var output = escape(iframe.body.innerText);
				output = output.replace("%3CP%3E%0D%0A%3CHR%3E", "%3CHR%3E");
				output = output.replace("%3CHR%3E%0D%0A%3C/P%3E", "%3CHR%3E");
				iframe.body.innerHTML = unescape(output);
			} else {
				var htmlSrc = iframe.body.ownerDocument.createRange();
				htmlSrc.selectNodeContents(iframe.body);
				iframe.body.innerHTML = htmlSrc.toString();
			}
			this.mode = 'html';
		}
	}

	function getIFrameDocument(id) {
		// if contentDocument exists, W3C compliant (Mozilla)
		if($(id).contentDocument) {
			return $(id).contentDocument;
		} else { // IE
			return document.frames[id].document;
		}
	}

	function insertAfter(newElement, targetElement) {
		var parent = targetElement.parentNode;
		if(parent.lastchild == targetElement) {
			parent.appendChild(newElement);
		} else {
			parent.insertBefore(newElement, targetElement.nextSibling);
		}
	}
	
	return function(id, options) {
		this.id = id;
		this.prefix = 'zaria-';
		this.frameId = this.prefix + this.id;
		this.options = options || defaults;
		this.mode = null;
		
		this.syncContent = syncContent;
		
		build.call(this);
	};
})();