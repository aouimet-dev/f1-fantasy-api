import requests
import undetected_chromedriver as uc
from selenium import webdriver
import os


chrome_options = webdriver.ChromeOptions()    
options = [
   "--window-size=1200,1200",
    "--ignore-certificate-errors",
    "--no-sandbox",
]

for option in options:
    chrome_options.add_argument(option)

driver = uc.Chrome(headless=True,use_subprocess=False, options=chrome_options)

driver.get('https://account.formula1.com')

cookies = driver.get_cookies()
token={"token": None}
for cookie in cookies:
    if cookie['name'] == 'reese84':
        token['token'] = cookie['value']

token = token['token']


cookies={
    'reese84': token,
    'login': '{"event":"login","componentId":"component_login_page","actionType":"success"}',
}

data={
    "Login": os.getenv('F1_EMAIL'), 
    "Password": os.getenv('F1_PASSWORD'),
    "DistributionChannel": "d861e38f-05ea-4063-8776-a7e2b6d885a4",
}

r = requests.post(
    'https://api.formula1.com/v2/account/subscriber/authenticate/by-password',
    headers={
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
        'Origin': 'https://account.formula1.com',
        'Referer': 'https://account.formula1.com',
        'Sec-Fetch-Dest': 'empty',
        'Sec-Fetch-Mode': 'cors',
        'Sec-Fetch-Site': 'same-site',
        'Sec-GPC': '1',
        'apiKey': 'fCUCjWrKPu9ylJwRAv8BpGLEgiAuThx7',
        'Accept': 'application/json, text/javascript, */*; q=0.01',
        'Accept-Encoding': 'gzip, deflate, br',
        'Content-Type': 'application/json',
        }, 
    json=data,
    cookies=cookies
)
print(r.status_code)
print(r.content)