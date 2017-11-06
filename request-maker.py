import asyncio
import requests

async def main():
    try:
        loop = asyncio.get_event_loop()
        futures = [
        loop.run_in_executor(
            None, 
            requests.get, 
            'http://localhost:8080'
        )
        for i in range(20)
    ]   
        for response in await asyncio.gather(*futures):
           print

    except Exception as e:
        raise e
    

loop = asyncio.get_event_loop()
loop.run_until_complete(main())