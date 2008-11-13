/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.ambrosia.api;

/**
 * CountdownTimer presents a countdown timer display, with text and graphic components, a warning zone near the end, and a possible action when done.
 */
public interface CountdownTimer extends Component
{
	/**
	 * Set the decision to be disabled (inactive, but visible).
	 * 
	 * @param decision
	 *        The decision, or set of decisions, all of which must pass to be disabled.
	 * @return self.
	 */
	CountdownTimer setDisabled(Decision... decision);

	/**
	 * Set the property reference for the total duration of the countdown in ms.
	 * 
	 * @param duration
	 *        The property reference for the total duration of the countdown in ms.
	 * @return self.
	 */
	CountdownTimer setDuration(PropertyReference duration);

	/**
	 * Set the text to preceed the total time display.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	CountdownTimer setDurationMessage(String selector, PropertyReference... references);

	/**
	 * Set the timer to submit the UI with this destination when it expires. Default behavior does not submit on expiration.
	 * 
	 * @param destination
	 *        The tool destination for after the submit.
	 * @return self.
	 */
	CountdownTimer setExpireDestination(Destination destination);

	/**
	 * Set the text for the hide button.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	CountdownTimer setHideMessage(String selector, PropertyReference... references);

	/**
	 * Set the decision to be included.
	 * 
	 * @param decision
	 *        The decision, or set of decisions, all of which must pass to be included.
	 * @return self.
	 */
	CountdownTimer setIncluded(Decision... decision);

	/**
	 * Set the text to preceed the remaining time display.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	CountdownTimer setRemainingMessage(String selector, PropertyReference... references);

	/**
	 * Set the text for the show button.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	CountdownTimer setShowMessage(String selector, PropertyReference... references);

	/**
	 * Indicate that the timer needs to submit the form when it fires.
	 * 
	 * @return self.
	 */
	CountdownTimer setSubmit();

	/**
	 * Set the display to be tight spaced for small timers.
	 * 
	 * @return self.
	 */
	CountdownTimer setTight();

	/**
	 * Set the property reference for the time from now till the countdown expires.
	 * 
	 * @param time
	 *        The property reference for the (in ms) from now till the countdown exipres.
	 * @return self.
	 */
	CountdownTimer setTimeTillExpire(PropertyReference time);

	/**
	 * Set the title message.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param references
	 *        one or more (or an array) of UiPropertyReferences to form the additional values in the formatted message.
	 * @return self.
	 */
	CountdownTimer setTitle(String selector, PropertyReference... references);

	/**
	 * Set the time in ms back from expiration for the area that the time shows in warning mode.
	 * 
	 * @param duration
	 *        The time in ms back from expiration for the warning zone.
	 * @return self.
	 */
	CountdownTimer setWarnDuration(long duration);

	/**
	 * Set the width of the graphic element, in pixels
	 * 
	 * @param width
	 *        Width in pixels.
	 * @return self.
	 */
	CountdownTimer setWidth(int width);
}
