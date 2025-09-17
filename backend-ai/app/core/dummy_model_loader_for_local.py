class DummyModelLoader:
    models = {}

    @classmethod
    def load_gen_model(cls):
        """생성 모델 로드를 모킹하는 함수"""
        print("더미 생성 모델 로드 완료")
        dummy_gen_model = "dummy_gen_model"
        dummy_gen_tokenizer = "dummy_gen_tokenizer"
        cls.models["gen"] = dummy_gen_model, dummy_gen_tokenizer
        return cls.models

    @classmethod
    def load_cls_model(cls):
        """분류 모델 로드를 모킹하는 함수"""
        print("더미 분류 모델 로드 완료")
        dummy_cls_model = "dummy_cls_model"
        dummy_cls_tokenizer = "dummy_cls_tokenizer"
        cls.models["cls"] = dummy_cls_model, dummy_cls_tokenizer
        return cls.models

    @classmethod
    def unload_gen_model(cls):
        """생성 모델 정리를 모킹하는 함수"""
        print("생성 모델 정리")

    @classmethod
    def unload_cls_model(cls):
        """분류 모델 정리를 모킹하는 함수"""
        print("분류 모델 정리")