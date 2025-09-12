from transformers import AutoTokenizer, AutoModelForCausalLM, BitsAndBytesConfig
from peft import PeftModel
import torch
import gc

class ModelLoader:
    GEN_MODEL_PATH = "MLP-KTLim/llama-3-Korean-Bllossom-8B".replace("/", "--")
    CLS_MODEL_PATH = "klue/bert-base".replace("/", "--")
    DOWNLOADED_PATH = "../models/downloaded_model/"
    FINETUNED_PATH = "../models/finetuned_model/"

    models = {}

    @classmethod
    def load_gen_model(cls):
        """서버 구동시에 호출할 템플릿 생성 모델을 메모리로 로드하는 함수"""

        if "gen" not in modles or modles["gen"] is None:
            base_model_path = cls.DOWNLOADED_PATH + cls.GEN_MODEL_PATH
            lora_adapter_path = cls.FINETUNED_PATH + cls.GEN_MODEL_PATH

            bnb_config = BitsAndBytesConfig(
                load_in_8bit=True,
                llm_int8_threshold=6.0
            )

            tokenizer = AutoTokenizer.from_pretrained(base_model_path)
            base_model = AutoModelForCausalLM.from_pretrained(
                base_model_path,
                quantization_config=bnb_config,
                device_map="auto",
                low_cpu_mem_usage=True
            )
            model = PeftModel.from_pretrained(base_model, lora_adapter_path)

            models["gen"] = model, tokenizer
            print("생성 모델 로드 완료")
            cls.check_gpu_memory_usage()
            return cls.models
        else:
            print("생성 모델이 이미 로드 되어 있습니다")
            cls.check_gpu_memory_usage()
            return cls.models

    @classmethod
    def unload_gen_model(cls):
        """생성 모델 메모리에서 정리"""
        cls.check_gpu_memory_usage()

        gen_model, gen_tokenizer = models.pop("gen")
        del gen_model
        del gen_tokenizer
        torch.cuda.empty_cache()
        gc.collect()

        cls.check_gpu_memory_usage()
        return

    @classmethod
    def load_cls_model(cls):
        """서버 구동시에 호출할 템플릿 검증 모델을 메모리로 로드하는 함수"""
        if "cls" not in modles or modles["cls"] is None:
            base_model_path = cls.DOWNLOADED_PATH + cls.CLS_MODEL_PATH
            finetuned_model_path = cls.FINETUNED_PATH + cls.CLS_MODEL_PATH

            tokenizer = AutoTokenizer.from_pretrained(base_model_path)
            model = AutoModelForSequenceClassification.from_pretrained(finetuned_model_path)
            model.eval()

            cls.models["cls"] = model, tokenizer
            print("분류 모델 로드 완료")
            cls.check_gpu_memory_usage()
            return cls.models
        else:
            print("분류 모델이 이미 로드 되어 있습니다")
            return cls.models
    @classmethod
    def unload_cls_model(cls):
        """메모리 정리"""
        cls.check_gpu_memory_usage()

        cls_model, cls_tokenizer = models.pop("cls")
        del cls_model
        del cls_tokenizer
        torch.cuda.empty_cache()
        gc.collect()

        check_gpu_memory_usage()
        return

    @staticmethod
    def check_gpu_memory_usage():
        # GPU가 사용 가능한지 확인
        if torch.cuda.is_available():
            # 현재 사용 중인 메모리 (바이트)
            allocated_bytes = torch.cuda.memory_allocated(device=0)
            # 캐시된 메모리 (바이트)
            reserved_bytes = torch.cuda.memory_reserved(device=0)

            # GB 단위로 변환
            gb_factor = 1024 * 1024 * 1024
            allocated_gb = allocated_bytes / gb_factor
            reserved_gb = reserved_bytes / gb_factor

            print(f"현재 사용 중인 GPU 메모리: {allocated_gb:.2f} GB")
            print(f"현재 캐시된 GPU 메모리: {reserved_gb:.2f} GB")
            # TODO 로깅으로 변경
        else:
            print("GPU를 사용할 수 없습니다.")