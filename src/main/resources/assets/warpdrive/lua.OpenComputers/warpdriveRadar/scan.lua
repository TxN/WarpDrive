local component = require("component")
local computer = require("computer")
local term = require("term")

if not term.isAvailable() then
  computer.beep()
  os.exit()
end
if component.gpu.getDepth() < 4 then
  print("Tier 2 GPU required")
  os.exit()
end

function error(message)
  component.gpu.setBackground(0x000000)
  component.gpu.setForeground(0xFF0000)
  local xt, yt = term.getCursor()
  component.gpu.set(xt, yt, message)
  component.gpu.setBackground(0x000000)
  component.gpu.setForeground(0xFFFFFF)
  print("")
end

if not component.isAvailable("warpdriveRadar") then
  computer.beep()
  error("No radar detected")
  os.exit()
end
local radar = component.warpdriveRadar

local argv = { ... }
if #argv ~= 1 then
  error("Usage: scan <scanRadius>")
  os.exit()
end

local radius = tonumber(argv[1])
local scale = 50

local w, h = component.gpu.getResolution()
local radarX, radarY, radarZ = radar.position()

term.clear()

function textOut(x, y, text, fg, bg)
  if term.isAvailable() then
    local w, _ = component.gpu.getResolution()
    if w then
      component.gpu.setBackground(bg)
      component.gpu.setForeground(fg)
      component.gpu.set(x, y, text)
      component.gpu.setBackground(0x000000)
    end
  end
end

function drawBox(x, y, width, height, color)
  if term.isAvailable() then
    local w, _ = component.gpu.getResolution()
    if w then
      component.gpu.setBackground(color)
      component.gpu.fill(x, y, width, height, " ")
      component.gpu.setBackground(0x000000)
    end
  end
end

function translateXZ(oldX, oldZ)
  local x = radarX - oldX
  local z = radarZ - oldZ
  
  x = x / (radius / scale)
  z = z / (radius / scale)
  
  x = x + (w / 2)
  z = z + (h / 2)
  
  x = math.floor(x)
  z = math.floor(z)
  
  return x, z
end

function drawContact(x, _, z, name, color)
  local newX, newZ = translateXZ(x, z)
  
  textOut(newX, newZ, " ", 0x000000, color)
  textOut(newX - 3, newZ + 1, "[" .. name .. "]", 0xFFFFFF, 0x000000)
end

function scanAndDraw()
  local energy, energyMax = radar.energy()
  if energy == nil then energy = 0 end
  if energyMax == nil or energyMax == 0 then energyMax = 1 end
  
  local energyRequired = radar.getEnergyRequired(radius)
  if energyRequired == nil then energyRequired = 0 end
  
  if (energyRequired <= 0 or energy < energyRequired) then
    local hh = math.floor(h / 2)
    local hw = math.floor(w / 2)
    
    drawBox(hw - 5, hh - 1, 11, 3, 0xFF0000)
    textOut(hw - 4, hh, "LOW POWER", 0xFFFFFF, 0xFF0000)
    os.sleep(1)
    
    return 0
  end
  
  radar.radius(radius)
  radar.start()
  local scanDuration = radar.getScanDuration(radius)
  os.sleep(scanDuration)
  
  redraw()
  
  local numResults = radar.getResultsCount()
  
  if (numResults ~= 0) then
    for i = 0, numResults-1 do
      local success, _, name, cx, cy, cz = radar.getResult(i)
      if success then
        drawContact(cx, cy, cz, name, 0xFF0000)
      end
    end
  end
  
  drawContact(radarX, radarY, radarZ, "RAD", 0xFFFF00)
end

function redraw()
  drawBox(2, 1, w - 2, h - 1, 0x00FF00)
  
  drawBox(1, 1, w, 1, 0x000000)
  drawBox(1, 1, 1, h, 0x000000)
  drawBox(1, h, w, 1, 0x000000)
  drawBox(w, 1, w, h, 0x000000)
  
  textOut((w / 2) - 8, 1, "= Q-Radar v0.3 =", 0xFFFFFF, 0x000000)
  
  textOut(w - 3, 1, "[X]", 0xFFFFFF, 0xFF0000)
  
  local energy, _ = radar.energy()
  if energy == nil then energy = 0 end
  textOut(4, h, "Energy: " .. energy .. " EU | Scan radius: " .. radius, 0xFFFFFF, 0x000000)
end

local continue = true
while component.isAvailable("warpdriveRadar") and continue do
  scanAndDraw()
end

term.clear()