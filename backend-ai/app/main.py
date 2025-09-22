from fastapi import FastAPI
from fastapi.responses import RedirectResponse
from .routers.template_router import template_router
from .routers.validate_router import validate_router
from .core.model_loader import model_loader
from .core.agent_initializer import AgentInitializer
import uvicorn
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Load model to memory
    model_loader.load_gen_model()
    model_loader.load_cls_model()

    AgentInitializer.initialize()
    yield
    # Unload model from memory
    model_loader.unload_gen_model()
    model_loader.unload_cls_model()

app = FastAPI(lifespan=lifespan)
app.include_router(template_router)
app.include_router(validate_router)

@app.get("/", include_in_schema=False)
async def redirect_to_docs():
    return RedirectResponse(url="/docs")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
