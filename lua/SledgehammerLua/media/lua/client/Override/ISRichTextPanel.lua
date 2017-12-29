require "ISUI/ISRichTextPanel"

function ISRichTextPanel:render()
    self.r = 1;
    self.g = 1;
    self.b = 1;
	if self.lines == nil then
		return;
	end
	if self.clip then self:setStencilRect(0, 0, self.width, self.height) end
	if self.textDirty then
		self:paginate();
	end
    local y = 0;
    local c = 1;
    for i,v in ipairs(self.images) do
        local h = self.imageY[c] + self.marginTop + self.imageH[c];
        if(h > y) then
            y = h;
        end
        self:drawTextureScaled(v, self.imageX[c] + self.marginLeft, self.imageY[c] + self.marginTop, self.imageW[c], self.imageH[c], 1, 1, 1, 1);
        c = c + 1;
    end
	c = 1;
    local orient = "left";
	y = 0;
	for i,v in ipairs(self.lines) do
		if self.lineY[c] + self.marginTop + self:getYScroll() >= self:getHeight() then
			break
		end
		if self.rgb[c] then
			self.r = self.rgb[c].r;
			self.g = self.rgb[c].g;
			self.b = self.rgb[c].b;
		end
		if self.orient[c] then
			orient = self.orient[c];
		end
		if self.fonts[c] then
			self.font = self.fonts[c];
		end
		if i == #self.lines or (self.lineY[c+1] + self.marginTop + self:getYScroll() > 0) then
			local r = self.r;
			local b = self.b;
			local g = self.g;
			local aFactor = self.alphaFactor;
			if aFactor == nil then
				aFactor = 1;
			end
			v = v:gsub("&lt;", "<")
			v = v:gsub("&gt;", ">")
			if string.trim(v) ~= "" then
				if orient == "centre" then
					if self.shadowText == true then
						self:drawTextCentre(string.trim(v), self.width / 2 + 2, self.lineY[c] + self.marginTop + 2, 0, 0, 0, 0.5 * aFactor, self.font);
						self:drawTextCentre(string.trim(v), self.width / 2 + 1, self.lineY[c] + self.marginTop + 1, 0, 0, 0, 0.8 * aFactor, self.font);
					end
					self:drawTextCentre(string.trim(v), self.width / 2 , self.lineY[c] + self.marginTop, r, g, b, aFactor, self.font);

				elseif orient == "right" then
					if self.shadowText == true then
						self:drawTextLeft(string.trim(v), self.lineX[c] + self.marginLeft + 2, self.lineY[c] + self.marginTop + 2, 0, 0, 0, 0.5 * aFactor, self.font);
						self:drawTextLeft(string.trim(v), self.lineX[c] + self.marginLeft + 1, self.lineY[c] + self.marginTop + 1, 0, 0, 0, 0.8 * aFactor, self.font);
					end				
					self:drawTextLeft( string.trim(v), self.lineX[c] + self.marginLeft, self.lineY[c] + self.marginTop, r, g, b, aFactor, self.font);
				else
					if self.shadowText == true then
						self:drawText(string.trim(v), self.lineX[c] + self.marginLeft + 2, self.lineY[c] + self.marginTop + 2, 0, 0, 0, 0.5 * aFactor, self.font);
						self:drawText(string.trim(v), self.lineX[c] + self.marginLeft + 1, self.lineY[c] + self.marginTop + 1, 0, 0, 0, 0.8 * aFactor, self.font);
					end
					self:drawText( string.trim(v), self.lineX[c] + self.marginLeft, self.lineY[c] + self.marginTop, r, g, b, aFactor, self.font);
				end
			end
		end
		local h = self.lineY[c] + self.marginTop + 32;
		if(h > y) then
			y = h;
		end
		c = c + 1;
	end
	if ISRichTextPanel.drawMargins then
		self:drawRectBorder(0, 0, self.width, self:getScrollHeight(), 0.5,1,1,1)
		self:drawRect(self.marginLeft, 0, 1, self:getScrollHeight(), 1,1,1,1)
		local maxLineWidth = self.maxLineWidth or (self.width - self.marginRight - self.marginLeft)
		self:drawRect(self.marginLeft + maxLineWidth, 0, 1, self:getScrollHeight(), 1,1,1,1)
		self:drawRect(0, self.marginTop, self.width, 1, 1,1,1,1)
		self:drawRect(0, self:getScrollHeight() - self.marginBottom, self.width, 1, 1,1,1,1)
	end
	if self.clip then self:clearStencilRect() end
end