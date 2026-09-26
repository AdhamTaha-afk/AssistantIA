import requests

def ask(q):
    r = requests.post("http://localhost:11434/api/generate", json={
        "model": "llama3",
        "prompt": q,
        "stream": False
    })
    return r.json()["response"]

print(ask("your question"))
