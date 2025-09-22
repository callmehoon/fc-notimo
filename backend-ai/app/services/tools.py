from langchain_core.tools import tool, ToolException

from typing import Annotated, List
from pydantic import ValidationError

from ..core.model_loader import model_loader
from ..schemas.template import Template, AgentOutput

@tool
def create_final_output(
        template: Annotated[
            Template,
            "A fully-formed and validated Pydantic Template object to be returned as the final output."
        ],
        chat_message: Annotated[
            str,
            "A brief, friendly, and conversational message to the user, summarizing the result."
        ]
) -> AgentOutput:
    """
    Combines a validated Template object and a conversational message into the final structured `AgentOutput`.
    This tool should be called as the final step in the agent's process to produce the complete and formatted response.
    """
    return AgentOutput(
        template=template,
        chat_message=chat_message
    )


@tool
def parse_template_from_generative_model_output(
    model_output: Annotated[
        str, "The raw output from a generative model that contains a JSON string, potentially with other text."
    ]
) -> Template:
    """
    Parses a JSON object from the raw output of a generative model and validates it against the Template Pydantic class.
    This tool is useful for extracting structured data when the model's output is not a pure JSON string and includes
    additional text, such as explanations, preambles, or markdown formatting (e.g., ```json{...}```).
    """
    try:
        start_idx = model_output.index("{")
        end_idx = model_output.rindex("}")
        template_str = model_output[start_idx:end_idx + 1]
        return Template.model_validate_json(template_str)
    except ValidationError as e:
        raise ToolException(f"Could not parse JSON string into Template Pydantic class: {e}")
    except ValueError as e:
        raise ToolException(f"Could not find a valid JSON object within the model's output: {e}")
    except Exception as e:
        raise ToolException(f"Failed to parse model's output: {e}")

@tool
def generate_template(
        original_template: Annotated[
            Template,
            ""
        ],
        user_input: Annotated[
            str,
            "The client's specific request or query to be used as a prompt for the template generation. "
            "This should be the main content provided by the user."
        ],
        related_policy: Annotated[
            List[str],
            "A list of specific policy documents or rules relevant to the user's request. "
            "This is essential for the model to generate an accurate and compliant template. "
            "The list can contain multiple strings, where each string is a different policy."
        ]
) -> str:
    """
    Generates a structured template in JSON format based on a user's request and a list of specific policies.
    This tool is designed to be called when a user needs a new template created according to predefined rules or constraints.
    The output is a raw string containing the generated JSON template.
    """
    import torch

    models = model_loader.models
    gen_model, gen_tokenizer = models.get("gen")

    prompt = f"original_template: {original_template}\nuser_input: {user_input}\npolicy: {related_policy}\ntemplate: "

    inputs = gen_tokenizer(prompt, return_tensors="pt", max_length=512, truncation=True).to(gen_model.device)

    with torch.no_grad():
        outputs = gen_model.generate(
            **inputs,
            max_new_tokens=150,
            temperature=0.3,
            do_sample=True,
            pad_token_id=gen_tokenizer.eos_token_id,
            early_stopping=True
        )

    response = gen_tokenizer.decode(outputs[0], skip_special_tokens=True)
    return response.split("template: ")[-1].strip()