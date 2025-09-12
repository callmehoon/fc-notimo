import os
from dotenv import load_dotenv

load_dotenv()
if os.getenv("IS_GPU_AVAILABLE") is "TRUE":
    IS_GPU_AVAILABLE = True
else:
    IS_GPU_AVAILABLE = False

if IS_GPU_AVAILABLE:
    from .actual_model_loader import ModelLoader
    model_loader = ModelLoader()
else:
    from .dummy_model_loader_for_local import DummyModelLoader
    model_loader = DummyModelLoader()