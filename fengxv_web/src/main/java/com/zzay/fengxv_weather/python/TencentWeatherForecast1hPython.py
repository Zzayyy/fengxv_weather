import sys

import requests
import re
import json

# 接收命令行参数
province = sys.argv[1]
city = sys.argv[2]


headers = {
    "Accept": "*/*",
    "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
    "Connection": "keep-alive",
    "Referer": "https://tianqi.qq.com/",
    "Sec-Fetch-Dest": "script",
    "Sec-Fetch-Mode": "no-cors",
    "Sec-Fetch-Site": "same-site",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Edg/136.0.0.0",
    "sec-ch-ua": "\"Chromium\";v=\"136\", \"Microsoft Edge\";v=\"136\", \"Not.A/Brand\";v=\"99\"",
    "sec-ch-ua-mobile": "?0",
    "sec-ch-ua-platform": "\"Windows\""
}
cookies = {
    "ptcz": "2e5c3cdf905b9b63498f30aa488948abe7066b7f77683f7ef40e70bd2c8b3c35",
    "pgv_pvid": "7639802060",
    "o_cookie": "1840238422",
    "qq_domain_video_guid_verify": "9ec23bbe80d76273",
    "_qimei_q36": "",
    "_qimei_h38": "52f732d245fc700010c33e220200000b617a16",
    "_qimei_fingerprint": "66d37e7fc1ab411e07b338b37089b0db",
    "_qimei_uuid42": "1860910221110032023b0d19629eb69f817e51d67e",
    "ied_qq": "o1840238422",
    "o2_uin": "1840238422",
    "eas_sid": "R1F7H2l2E9S5D163u3O7b5W0U9",
    "uin_cookie": "o1840238422",
    "RK": "f/ul9icDEc",
    "pac_uid": "0_GRtm1NEwG62Dx",
    "suid": "user_0_GRtm1NEwG62Dx",
    "fqm_pvqid": "91ce06d1-ca23-4645-bffd-48fa95352970",
    "_clck": "t80v7u|1|frf|0",
    "logTrackKey": "fcb2535facea4cbaafa38df6269f91de",
    "omgid": "0_GRtm1NEwG62Dx",
    "pgv_info": "ssid=s1917490065"
}
url = "https://wis.qq.com/weather/common"
params = {
    "source": "pc",
    "weather_type": "observe|forecast_1h|forecast_24h|index|alarm|limit|tips|rise",
    "province": province,
    "city": city,
    "county": "",
    "callback": "jQuery111305837889338525245_1747829888991",
    "_": "1747829888993"
}
response = requests.get(url, headers=headers, cookies=cookies, params=params)

# print(response.text)
# print(response)

# match = re.search(r'\((.*?)\)', response.text)[1]
# match = json.loads(match)
#
# forecast_1h = match["data"]["forecast_1h"]
# forecast_24h = match["data"]["forecast_24h"]
# rise = match["data"]["rise"]

# print(forecast_1h)
# print(forecast_24h)
# print(rise)


match = re.search(r'\((.*?)\)', response.text).group(1)
data = json.loads(match)

# 输出 JSON 格式的数据给 Java
print(json.dumps({
    "forecast_1h": data["data"]["forecast_1h"]
#     "forecast_24h": data["data"]["forecast_24h"],
#     "rise": data["data"]["rise"]
}))






