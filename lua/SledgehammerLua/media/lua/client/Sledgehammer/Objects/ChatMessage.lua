-- This file is part of Sledgehammer.
--
--    Sledgehammer is free software: you can redistribute it and/or modify
--    it under the terms of the GNU Lesser General Public License as published by
--    the Free Software Foundation, either version 3 of the License, or
--    (at your option) any later version.
--
--    Sledgehammer is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU Lesser General Public License for more details.
--
--    You should have received a copy of the GNU Lesser General Public License
--    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
--
--	Sledgehammer is free to use and modify, ONLY for non-official third-party servers 
--    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors. 

require "Class"

----------------------------------------------------------------
-- ChatMessage.lua
--
-- @module Core
-- @author Jab
-- @license LGPL
--
-- @string channel 	Channel the message is being broadcasted.
-- @string message 	The content being broadcasted.
----------------------------------------------------------------
ChatMessage = class(function(o, channel, message)
	
	-- The ID of the message. This serves as the initial timestamp too.
	o.messageID = getTimestamp();
	
	-- The channel the message is broadcasted.
	o.channel = channel;

	-- The original channel the message was broadcasted.
	o.channelInitial = channel;

	-- The content of the message.
	o.message = message;

	-- The unedited version of the message.
	o.messageInitial = message;

	-- If the message is moved to another channel.
	o.moved = false;

	-- The ID of the player who moved the message. 
	o.moverID = nil;

	-- If the message has been edited.
	o.edited = false;

	-- The ID of the player who edited the message.
	o.editorID = nil;

	-- If the message has been deleted.
	o.deleted = false;

	-- The timestamp for the message being edited, or deleted.
	o.editedTime = -1;

end);

----------------------------------------------------------------
-- @return 	Returns the ID of the message.
----------------------------------------------------------------
function ChatMessage:getID()
	return self.messageID;
end

----------------------------------------------------------------
-- @return 	Returns the channel's name being broadcasted.
----------------------------------------------------------------
function ChatMessage:getChannel()
	return self.channel;
end

----------------------------------------------------------------
-- @return 	Returns whether or not the message was edited.
----------------------------------------------------------------
function ChatMessage:isEdited()
	return self.edited;
end

----------------------------------------------------------------
-- @return 	Returns whether or not the message was deleted.
----------------------------------------------------------------
function ChatMessage:isDeleted()
	return self.deleted;
end

----------------------------------------------------------------
-- @return 	Returns whether or not the message was moved.
----------------------------------------------------------------
function ChatMessage:isMoved()
	return self.moved;
end

----------------------------------------------------------------
-- @return 	Returns the Player that moved the message. (ID).
----------------------------------------------------------------
function ChatMessage:getMover()
	return self.mover;
end

----------------------------------------------------------------
-- @return 	Returns the timestamp for when the message was edited.
----------------------------------------------------------------
function ChatMessage:getEditedTimestamp()
	return self.editedTime;
end

----------------------------------------------------------------
-- Sets the channel the message is being broadcasted to.
--
-- @string channel 	The channel being broadcasted.
----------------------------------------------------------------
function ChatMessage:setChannel(channel)

	-- Validity check.
	if channel == nil or channel == "" then
		print("Channel given is null or empty!");
		return;
	end

	-- Make sure the origin and destination are not the same.
	if self.channel ~= channel then

		-- Set the new channel.
		self.channel = channel;

		-- Notify being moved.
		self.moved = true;

		-- TODO: Get mover.
	end

end

----------------------------------------------------------------
-- Sets the message as being edited.
--
-- @bool flag 	True or False.
----------------------------------------------------------------
function ChatMessage:setEdited(flag)
	self.edited = flag;

	-- If edited is true
	if self.edited then

		-- Set the timestamp of the edit.
		self.editedTime = getTimestamp();

		-- TODO: get editor.
	else

		-- Reset the timestamp.
		self.editedTime = -1;

		-- Reset the editor.
		self.editor = nil;
	end
end

----------------------------------------------------------------
-- Deletes the message.
----------------------------------------------------------------
function ChatMessage:delete()
	if SledgeHammer.isDebug() then 
		print("Message deleted: " .. tostring(self:getID()) .. " in channel: " .. tostring(self:getChannel()));
	end
	self.edited = true;
	self.deleted = true;
end

----------------------------------------------------------------
-- Sets the message content.
--
-- @string message 	The content being broadcasted.
----------------------------------------------------------------
function ChatMessage:setMessage(message)
	
	-- Sending a null message triggers the deleteMessage code.
	if message == nil then
		self:delete();
		return;
	end

	-- Check to make sure a duplicate entry is not occuring.
	if message ~= self:getMessage() then

		-- Set the new message content.
		self.message = message;


		self.edited = true;
		self.editedTime = getTimestamp();
	end	
end