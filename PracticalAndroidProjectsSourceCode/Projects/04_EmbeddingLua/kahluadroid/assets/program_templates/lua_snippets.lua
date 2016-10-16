-- KahluaDroid
-- Sample Lua Snippets
-- 2010.12.20
-- http://pietergreyling.com

-- android_sdk()
if android_sdk() == "9" then 
  print "Gingerbread"  
end

-- android_release()
print("Android Release: "..android_release())

-- android_alert("text", "title")

android_alert(
  "Run me on the GUI thread!", 
  "Android Alert")

-- android_toast()
android_toast("Run me on the GUI thread!")

-- android_notify(
--   title, tickerText, message)
android_notify(
  "KahluaDroid Notification", 
  "KahluaDroid message waiting...", 
  "Thanks for reading this message.")

-- app_settextsize() / app_settextcolor()
app_settextsize(1)  -- small 
app_settextcolor(3) -- grey on blue
app_settextsize(2)  -- normal
app_settextcolor(2) -- green on dkgrey
app_settextsize(3)  -- large
app_settextcolor(1) -- black on white

-- lua runtime lib: os
print(os.date())

-- lua runtime lib: math
print(math.sin(3))

-- tables
a={}; a["num"]=12345; print("n="..a["num"])

-- functions
function func(a) 
  print("-- func: "..a)
end
func ("test argument")

-- random number generation
local r = newrandom()
r:random() -- 0 to 1
r:random(max) -- 1 to max
r:random(min, max) -- min to max
-- seeds with hashcode of object
r:seed(obj)

---[[
local r = newrandom()
for i=1,6 do
  print("dice "..i.." rolled "..r:random(6))
end
--]]

-- Sample Startup Script
msg1 = "Startup script complete.\n"
msg2 = "KahluaDroid ready..."
app_settextsize(1)  -- small
app_settextcolor(3) -- grey on blue
print(msg1..msg2)
android_toast(msg1..msg2)
