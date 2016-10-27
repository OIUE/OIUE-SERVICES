service.pid="org.oiue.service.tcp.bytes.Activator"
idleTime="90000"
listenPort="8070"
listenAddress="0.0.0.0"
charset="UTF-8"
sessions_key="_system_online_"

0x0100
msg.type.256="token_id,String,36|tag,String,-1"
msg.modulename.256="message_module"
msg.operation.256="passageway"

0x200
msg.type.512="target_type,int,1|target,String,36|send_time,int,4|msg_type,int,1|msg_size,int,2|msg,byte,gmsg_size"
msg.modulename.512="message_module"
msg.operation.512="send_msg"

0x8401
msg.type.33793="target_token,String,36|group_type,int,1|group_token,String,36|group_info,String,-1"
msg.modulename.33793="message_module"
msg.operation.33793="send_msg"

0x8402
msg.type.33794="group_token,String,36|group_info,String,-1"
msg.modulename.33794="message_module"
msg.operation.33794="send_msg"

0x8301
msg.type.33537="target_type,int,1|target_token,String,36|group_type,int,1|group_token,String,36|group_info,String,-1"
msg.modulename.33537="message_module"
msg.operation.33537="send_msg"

0x8302
msg.type.33538="target_type,int,1|target_token,String,36|group_type,int,1|group_token,String,36|group_info,String,-1"
msg.modulename.33538="message_module"
msg.operation.33538="send_msg"

0x8201
msg.type.33281="target_type,int,1|target_token,String,36|group_type,int,1|group_token,String,36|send_time,int,4|msg_type,int,1|msg_size,int,2|msg,byte,gmsg_size"
msg.modulename.33281="message_module"
msg.operation.33281="send_msg"
