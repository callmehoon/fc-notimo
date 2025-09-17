from langchain.agents.output_parsers.tools import ToolAgentAction

from ..schemas.template import TemplateRequest, TemplateResponse, Template
from ..core.agent_initializer import AgentInitializer

async def gen_template(request: TemplateRequest) -> TemplateResponse:
    agent_executor = AgentInitializer.agent_executor

    result = await agent_executor.ainvoke({
        "original_template": request.original_template.model_dump_json(indent=2),
        "user_input": request.user_input,
        "related_policy": request.related_policy,
    })

    agent_output = None

    for action, observation in reversed(result.get('intermediate_steps', [])):
        if isinstance(action, ToolAgentAction) and action.tool == 'create_final_output':
            agent_output = observation
            break

    return TemplateResponse(
        template=agent_output.template,
        chat_response=agent_output.chat_message
    )