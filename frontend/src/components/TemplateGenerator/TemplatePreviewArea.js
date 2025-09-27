import React from 'react';
import {Box, Typography, Button, Paper, Chip, CircularProgress, Tooltip} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import ErrorIcon from '@mui/icons-material/Error';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import {useNavigate} from 'react-router-dom';

function TemplatePreviewArea({template, validationResult, validationLoading, isPreviewingHistory, onReturnToLatest}) {
    const navigate = useNavigate();

    const handleGoBack = () => {
        navigate('/mytemplate');
    };

    const renderValidationResult = () => {
        if (validationLoading) {
            return (
                <Box sx={{display: 'flex', alignItems: 'center', gap: 1, mt: 2}}>
                    <CircularProgress size={20}/>
                    <Typography variant="body2">승인 검증 중...</Typography>
                </Box>
            );
        }

        if (!validationResult) return null;

        const {result, probability} = validationResult;

        if (result === 'approve') {
            return (
                <Chip
                    icon={<CheckCircleIcon/>}
                    label={`승인 예상 (${probability})`}
                    color="success"
                    variant="outlined"
                    sx={{mt: 2}}
                />
            );
        } else if (result === 'reject') {
            return (
                <Chip
                    icon={<CancelIcon/>}
                    label={`거부 예상 (${probability})`}
                    color="error"
                    variant="outlined"
                    sx={{mt: 2}}
                />
            );
        } else if (result === 'error') {
            return (
                <Chip
                    icon={<ErrorIcon/>}
                    label={probability}
                    color="warning"
                    variant="outlined"
                    sx={{mt: 2}}
                />
            );
        }

        return null;
    };

    return (
        <div style={{display: 'flex', flexDirection: 'column', height: '100%', padding: '10px'}}>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px'}}>
                <Box sx={{display: 'flex', alignItems: 'center', gap: 1}}>
                    <Typography variant="h6">템플릿 미리보기</Typography>
                    {isPreviewingHistory && (
                        <Button
                            onClick={onReturnToLatest}
                            size="small"
                            variant="text"
                            sx={{ml: 1}}
                        >
                            (현재 템플릿으로 돌아가기)
                        </Button>
                    )}
                    <Typography variant="caption"
                                sx={{color: 'text.secondary', opacity: 0.7, ml: isPreviewingHistory ? 0 : 1}}>
                        템플릿은 자동 저장됩니다.
                    </Typography>
                </Box>
                <Button
                    variant="outlined"
                    startIcon={<ArrowBackIcon/>}
                    onClick={handleGoBack}
                    size="small"
                    sx={{color: 'text.secondary', borderColor: 'text.secondary'}}
                >
                    돌아가기
                </Button>
            </div>
            <Box sx={{
                flexGrow: 1,
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
                padding: '20px',
                overflowY: 'auto',
            }}>
                {template ? (
                    <Box
                        sx={{
                            width: '100%',
                            maxWidth: '400px',
                            minHeight: '500px',
                            display: 'flex',
                            flexDirection: 'column'
                        }}
                    >
                        {/* TemplateCard 스타일의 알림톡 미리보기 */}
                        <Box
                            sx={{
                                position: 'relative',
                                background: 'linear-gradient(135deg, #1890ff 0%, #40a9ff 100%)',
                                borderRadius: '16px',
                                padding: '20px',
                                margin: '8px',
                                flexGrow: 1,
                                display: 'flex',
                                flexDirection: 'column',
                                boxShadow: '0 8px 32px rgba(24, 144, 255, 0.15)',
                                border: '1px solid rgba(255, 255, 255, 0.18)',
                                backdropFilter: 'blur(10px)',
                                transition: 'all 0.3s ease',
                                '&:hover': {
                                    transform: 'translateY(-4px)',
                                    boxShadow: '0 12px 40px rgba(24, 144, 255, 0.25)',
                                },
                                '&::before': {
                                    content: '""',
                                    position: 'absolute',
                                    bottom: '-8px',
                                    left: '24px',
                                    width: 0,
                                    height: 0,
                                    borderLeft: '10px solid transparent',
                                    borderRight: '10px solid transparent',
                                    borderTop: '10px solid #1890ff',
                                    filter: 'drop-shadow(0 2px 4px rgba(24, 144, 255, 0.2))',
                                }
                            }}
                        >
                            {/* 상단 헤더: 제목 */}
                            <Box sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'flex-start',
                                mb: 2
                            }}>
                                <Box sx={{display: 'flex', alignItems: 'center', flex: 1, mr: 1}}>
                                    <Box
                                        sx={{
                                            width: '32px',
                                            height: '32px',
                                            borderRadius: '8px',
                                            backgroundColor: 'rgba(255, 255, 255, 0.9)',
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            mr: 1.5,
                                            boxShadow: '0 2px 8px rgba(255, 255, 255, 0.2)'
                                        }}
                                    >
                                        <Typography sx={{fontSize: '16px'}}>📧</Typography>
                                    </Box>
                                    <Typography
                                        variant="subtitle2"
                                        sx={{
                                            fontWeight: 600,
                                            fontSize: '15px',
                                            color: 'white',
                                            fontFamily: '"Spoqa Han Sans Neo", -apple-system, BlinkMacSystemFont, sans-serif',
                                            display: '-webkit-box',
                                            WebkitLineClamp: 1,
                                            WebkitBoxOrient: 'vertical',
                                            overflow: 'hidden',
                                            textOverflow: 'ellipsis',
                                            textShadow: '0 1px 2px rgba(0, 0, 0, 0.1)'
                                        }}
                                    >
                                        {template.title || template.individualTemplateTitle || '제목 없음'}
                                    </Typography>
                                </Box>
                            </Box>

                            {/* 메시지 내용 */}
                            <Box
                                sx={{
                                    backgroundColor: 'rgba(255, 255, 255, 0.95)',
                                    borderRadius: '12px',
                                    padding: '16px',
                                    mb: 2,
                                    flexGrow: 1,
                                    border: '1px solid rgba(255, 255, 255, 0.2)',
                                    backdropFilter: 'blur(10px)',
                                    boxShadow: 'inset 0 1px 3px rgba(0, 0, 0, 0.1)',
                                }}
                            >
                                <Tooltip
                                    title={template.text || template.content || template.individualTemplateContent || '내용 없음'}
                                    arrow
                                    placement="top"
                                    componentsProps={{
                                        tooltip: {
                                            sx: {
                                                maxWidth: '350px',
                                                fontSize: '13px',
                                                lineHeight: 1.6,
                                                whiteSpace: 'pre-line',
                                                backgroundColor: 'rgba(24, 144, 255, 0.95)',
                                                color: 'white',
                                                borderRadius: '12px',
                                                padding: '16px',
                                                boxShadow: '0 8px 32px rgba(24, 144, 255, 0.3)',
                                                fontFamily: '"Spoqa Han Sans Neo", sans-serif'
                                            }
                                        },
                                        arrow: {
                                            sx: {
                                                color: 'rgba(24, 144, 255, 0.95)'
                                            }
                                        }
                                    }}
                                >
                                    <Typography
                                        variant="body2"
                                        sx={{
                                            color: '#2c3e50',
                                            fontSize: '14px',
                                            lineHeight: 1.6,
                                            fontFamily: '"Spoqa Han Sans Neo", -apple-system, BlinkMacSystemFont, sans-serif',
                                            whiteSpace: 'pre-line',
                                            cursor: 'help',
                                            fontWeight: 400
                                        }}
                                    >
                                        {template.text || template.content || template.individualTemplateContent || '내용 없음'}
                                    </Typography>
                                </Tooltip>
                            </Box>

                            {/* 자세히보기 버튼 */}
                            {(template.button_name || template.buttonTitle) && (
                                <Box sx={{display: 'flex', justifyContent: 'center', mb: 2}}>
                                    <Button
                                        variant="contained"
                                        size="small"
                                        disabled
                                        sx={{
                                            backgroundColor: 'rgba(255, 255, 255, 0.9)',
                                            color: '#1890ff',
                                            fontSize: '13px',
                                            fontWeight: 600,
                                            textTransform: 'none',
                                            borderRadius: '20px',
                                            px: 3,
                                            py: 0.8,
                                            boxShadow: '0 2px 8px rgba(255, 255, 255, 0.3)',
                                            '&:disabled': {
                                                backgroundColor: 'rgba(255, 255, 255, 0.8)',
                                                color: '#1890ff'
                                            }
                                        }}
                                    >
                                        {template.button_name || template.buttonTitle}
                                    </Button>
                                </Box>
                            )}

                            {/* 하단 메타 정보 */}
                            <Box sx={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                mt: 'auto',
                                pt: 1
                            }}>
                                <Box sx={{display: 'flex', alignItems: 'center', gap: 1}}>
                                    <Box sx={{
                                        backgroundColor: 'rgba(255, 255, 255, 0.2)',
                                        borderRadius: '12px',
                                        px: 1.5,
                                        py: 0.5
                                    }}>
                                        <Typography variant="caption" sx={{
                                            fontSize: '11px',
                                            color: 'rgba(255, 255, 255, 0.9)',
                                            fontWeight: 500,
                                            fontFamily: '"Spoqa Han Sans Neo", sans-serif'
                                        }}>
                                            알림톡
                                        </Typography>
                                    </Box>
                                </Box>
                                <Typography variant="caption" sx={{
                                    fontSize: '11px',
                                    color: 'rgba(255, 255, 255, 0.8)',
                                    fontWeight: 400
                                }}>
                                    Preview
                                </Typography>
                            </Box>
                        </Box>

                        {/* 검증 결과 표시 */}
                        <Box sx={{mt: 2, textAlign: 'center'}}>
                            {renderValidationResult()}
                        </Box>
                    </Box>
                ) : (
                    <Typography variant="body2" color="text.secondary" sx={{textAlign: 'center'}}>
                        AI와 채팅을 시작하면 여기에 템플릿 미리보기가 표시됩니다.
                    </Typography>
                )}
            </Box>
        </div>
    );
}

export default TemplatePreviewArea;
