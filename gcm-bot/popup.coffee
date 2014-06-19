
onCodes = [
  'var index = 0'
  'g._bot_ = true'
  'function _botFunc_() {'
  '  g.keys[37] = g.keys[38] = g.keys[39] = g.keys[40] = false'
  '  if (g._bot_ == false) return'
  '  g.keys[37 + (index++ % 4)] = true'
  '  setTimeout(_botFunc_, 500)'
  '}'
  '_botFunc_()'
  ]

onCode = onCodes.join ';'
offCode = "g._bot_ = false;"

makeCode = (innerCode) ->
  """
var script = document.createElement("script");
script.appendChild(document.createTextNode("#{innerCode}"));
document.body.appendChild(script);
document.body.removeChild(script);
"""

unless chrome.extension.getBackgroundPage().isWandering
  chrome.extension.getBackgroundPage().isWandering = true
  chrome.tabs.query
    active: true
    , (tabs) ->
      chrome.tabs.executeScript { code: makeCode(onCode) }, (ret) ->
        document.body.appendChild document.createTextNode 'Started'
else
  chrome.extension.getBackgroundPage().isWandering = false
  chrome.tabs.query
    active: true
    , (tabs) ->
      chrome.tabs.executeScript { code: makeCode(offCode) }, (ret) ->
        document.body.appendChild document.createTextNode 'Stopped'
