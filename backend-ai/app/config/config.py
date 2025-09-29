import os
import boto3
import chromadb

from dotenv import load_dotenv
from pathlib import Path
from langchain_openai import OpenAIEmbeddings
from langchain_chroma import Chroma

load_dotenv()
OPENAI_API_KEY = os.environ["OPENAI_API_KEY"]

HERE = Path(__file__).resolve()     # .../app/config/config.py
APP_DIR = HERE.parents[1]           # .../app

CHROMA_DB_DIR   = str(APP_DIR / "template_guide_db")         # app/template_guide_db
COLLECTION_NAME = "template_guide"

S3_BUCKET = os.getenv("S3_BUCKET", "dr.hong-s3")
S3_KEY    = os.getenv("S3_KEY", "dataset/Guidelines.txt")

def s3_text(bucket: str = S3_BUCKET, key: str = S3_KEY, encoding: str = "utf-8") -> str:
    s3 = boto3.client("s3")
    resp = s3.get_object(Bucket=bucket, Key=key)
    return resp["Body"].read().decode(encoding)

_embeddings = OpenAIEmbeddings(model="text-embedding-3-large")

_client = chromadb.PersistentClient(path=CHROMA_DB_DIR)

vector_store = Chroma(
    client=_client,
    collection_name=COLLECTION_NAME,
    embedding_function=_embeddings,
)