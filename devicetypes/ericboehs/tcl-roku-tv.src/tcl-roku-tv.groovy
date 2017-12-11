/* Code is here: https://github.com/tcurtin/smartthings-roku-tv
 * originally from: https://github.com/ericboehs/smartthings-roku-tv
 * 
 * According to https://www.miniwebtool.com/ip-address-to-hex-converter/?ip=192.168.0.220 the ip 192.168.0.220 
 * (which is reserved in router for mac of roku) is C0A800DC.  Port 8060 is 1F7C. 
 * So C0A800DC:1F7C needs to be entered when adding device under My Devices in the smartthings website
 */


preferences {
    input("deviceIp", "text", title: "Device IP")
    input("deviceMac", "text", title: "Device MAC Address")
}

metadata {
  definition (name: "TCL Roku TV", namespace: "ericboehs", author: "Eric Boehs") {
    capability "Switch"
    capability "Polling"
    capability "Refresh"
  }

  simulator {
  }

  tiles {
    standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
      state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
      state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
    }

    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }

    main "button"
    details(["button", "refresh"])
  }
}

def installed() {
  updated()
}

def updated() {
  log.debug "updated"
  poll()
  runEvery1Minute(poll)
}

def parse(String description) {
  def msg = parseLanMessage(description)

  if (msg.body && msg.body.contains("PowerOn")) {
    log.debug "TV on"
    sendEvent(name: "switch", value: "on")
  }
}

def on() {
  log.debug "Executing 'on'"
  sendEvent(name: "switch", value: "on")

  sendHubCommand(new physicalgraph.device.HubAction (
    "wake on lan ${deviceMac}",
    physicalgraph.device.Protocol.LAN,
    null,
    [:]
  ))

  def postResult = new physicalgraph.device.HubAction(
    method: "POST",
    path: "/keypress/Power",
    headers: [ HOST: "${deviceIp}:8060" ],
  )
}

def off() {
  log.debug "Executing 'off'"
  sendEvent(name: "switch", value: "off")
  def result = new physicalgraph.device.HubAction(
    method: "POST",
    path: "/keypress/PowerOff",
    headers: [ HOST: "${deviceIp}:8060" ],
  )
}

def poll() {
  log.debug "Executing 'poll'"
  refresh()
}

def refresh() {
  log.debug "Executing 'refresh'"
  queryDeviceState()
}

def queryDeviceState() {
  sendEvent(name: "switch", value: "off")
  sendHubCommand(new physicalgraph.device.HubAction(
    method: "GET",
    path: "/query/device-info",
    headers: [ HOST: "${deviceIp}:8060" ]
  ))
}