from fastapi import FastAPI
from fastapi.responses import RedirectResponse
from .routers.template_router import template_router
from .routers.validate_router import validate_router
from .core.model_loader import models, load_gen_model, load_cls_model
import uvicorn
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    # TODO Load model to memory
    gen_model = load_gen_model()
    cls_model = load_cls_model()
    models["gen_model"] = gen_model
    models["cls_model"] = cls_model
    yield
    # TODO Unload model from memory
    unload_gen_model()
    unload_cls_model()

app = FastAPI(lifespan=lifespan)
app.include_router(template_router)
app.include_router(validate_router)

@app.get("/", include_in_schema=False)
async def redirect_to_docs():
    return RedirectResponse(url="/docs")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
