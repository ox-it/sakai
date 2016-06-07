/*
 * JuiceBasicPanel 0.1 - Javascript User Interface Componentised Extension
 * http://juice-project.googlecode.com
 *
 * Copyright (c) 2009 Talis (talis.com)
 * Originator: Richard Wallis
 * Under GPL (gpl-2.0.txt) license.
 *
 * $Author: richard.wallis@talis.com $
 * $Date: 2009-02-22 00:59:06 +0000 (Sun, 22 Feb 2009) $
 * $Rev: 2 $
 */
function JuiceBasicPanel(insertDiv, panelId, startClass, liveClass, showFunc){
	JuiceBasicPanel.superclass.init.call(this,insertDiv, panelId, startClass, liveClass, showFunc);
}

JuiceBasicPanel.prototype = new JuicePanel();
JuiceBasicPanel.prototype.constructor = JuiceBasicPanel;
JuiceBasicPanel.superclass = JuicePanel.prototype;
