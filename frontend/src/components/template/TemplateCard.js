// src/components/template/TemplateCard.jsx
import { Card, CardContent, CardActions, IconButton, Typography, Tooltip, Button, Box, Chip } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import ShareIcon from '@mui/icons-material/Share';
import EditIcon from '@mui/icons-material/Edit';
import FavoriteIcon from '@mui/icons-material/Favorite';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';

export default function TemplateCard({ template, onDelete, onShare, onEdit, onUse, showActions, isPublicTemplate, onFavorite, isFavorite }) {
    
    const title = (template.individualTemplateTitle?.trim() || 
                  template.publicTemplateTitle?.trim() || 
                  template.title?.trim()) || '제목 없음';
    const content = (template.individualTemplateContent?.trim() || 
                    template.publicTemplateContent?.trim() || 
                    template.content?.trim()) || '내용 없음';
    const buttonTitle = template.buttonTitle;
    const userRole = localStorage.getItem('userRole');
    
    // 상태값에 따른 스타일 설정
    const getStatusChip = (status) => {
        if (!status) return null;
        
        const statusConfig = {
            'DRAFT': { label: '작성중', color: 'default' },
            'PENDING': { label: '심사중', color: 'warning' },
            'APPROVED': { label: '승인됨', color: 'success' },
            'REJECTED': { label: '반려됨', color: 'error' }
        };
        
        const config = statusConfig[status] || { label: status, color: 'default' };
        
        return (
            <Chip
                label={config.label}
                color={config.color}
                size="small"
                sx={{ fontSize: '0.7rem', height: '20px' }}
            />
        );
    };

    return (
        <Box
            sx={{
                width: '100%',
                maxWidth: '100%',
                minHeight: '200px',
                display: 'flex',
                flexDirection: 'column'
            }}
        >
            {/* 현대적인 알림톡 스타일 메시지 카드 */}
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
                {/* 상단 헤더: 제목과 상태 */}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', flex: 1, mr: 1 }}>
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
                            <Typography sx={{ fontSize: '16px' }}>📧</Typography>
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
                            {title}
                        </Typography>
                    </Box>
                    {!isPublicTemplate && (
                        <Box sx={{
                            backgroundColor: 'rgba(255, 255, 255, 0.15)',
                            borderRadius: '12px',
                            px: 1,
                            py: 0.5
                        }}>
                            {getStatusChip(template.status)}
                        </Box>
                    )}
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
                        title={content}
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
                                display: '-webkit-box',
                                WebkitLineClamp: 4,
                                WebkitBoxOrient: 'vertical',
                                overflow: 'hidden',
                                textOverflow: 'ellipsis',
                                whiteSpace: 'pre-line',
                                cursor: 'help',
                                fontWeight: 400
                            }}
                        >
                            {content}
                        </Typography>
                    </Tooltip>
                </Box>

                {/* 자세히보기 버튼 */}
                {isPublicTemplate && buttonTitle && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
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
                            {buttonTitle}
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
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
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
                        Template
                    </Typography>
                </Box>
            </Box>

            {/* 하단 액션 영역 - 현대적인 스타일 */}
            <Box sx={{
                mx: 1,
                mb: 2,
                display: 'flex',
                alignItems: 'center',
                gap: 1,
                backgroundColor: 'rgba(24, 144, 255, 0.08)',
                borderRadius: '16px',
                p: 2,
                border: '1px solid rgba(24, 144, 255, 0.12)',
                backdropFilter: 'blur(10px)'
            }}>
                {/* 즐겨찾기 버튼 */}
                {onFavorite && (
                    <Tooltip title={isFavorite ? "즐겨찾기 해제" : "즐겨찾기 추가"}>
                        <IconButton
                            onClick={onFavorite}
                            size="small"
                            sx={{
                                backgroundColor: isFavorite ? '#FF6B9D' : 'rgba(255, 255, 255, 0.9)',
                                color: isFavorite ? '#FFFFFF' : '#757575',
                                border: isFavorite ? '2px solid #FF1744' : '1px solid #E0E0E0',
                                boxShadow: isFavorite ? '0 4px 12px rgba(255, 107, 157, 0.4)' : '0 2px 4px rgba(0, 0, 0, 0.1)',
                                '&:hover': {
                                    backgroundColor: isFavorite ? '#FF8BA7' : '#F5F5F5',
                                    transform: 'scale(1.1)',
                                    boxShadow: isFavorite ? '0 6px 16px rgba(255, 107, 157, 0.5)' : '0 4px 8px rgba(0, 0, 0, 0.15)'
                                },
                                transition: 'all 0.3s ease-in-out',
                                '&:active': {
                                    transform: 'scale(0.95)'
                                }
                            }}
                        >
                            {isFavorite ? <FavoriteIcon fontSize="small" /> : <FavoriteBorderIcon fontSize="small" />}
                        </IconButton>
                    </Tooltip>
                )}

                {/* 개인 템플릿의 액션 버튼들 */}
                {!isPublicTemplate && showActions && (
                    <>
                        <Tooltip title="공유하기">
                            <IconButton
                                onClick={onShare}
                                size="small"
                                sx={{
                                    backgroundColor: '#E3F2FD',
                                    color: '#1976D2',
                                    border: '1px solid #BBDEFB',
                                    '&:hover': {
                                        backgroundColor: '#BBDEFB',
                                        transform: 'scale(1.05)'
                                    },
                                    transition: 'all 0.2s ease-in-out'
                                }}
                            >
                                <ShareIcon fontSize="small" />
                            </IconButton>
                        </Tooltip>
                        <Tooltip title="수정하기">
                            <IconButton
                                onClick={onEdit}
                                size="small"
                                sx={{
                                    backgroundColor: '#F3E5F5',
                                    color: '#7B1FA2',
                                    border: '1px solid #E1BEE7',
                                    '&:hover': {
                                        backgroundColor: '#E1BEE7',
                                        transform: 'scale(1.05)'
                                    },
                                    transition: 'all 0.2s ease-in-out'
                                }}
                            >
                                <EditIcon fontSize="small" />
                            </IconButton>
                        </Tooltip>
                        <Tooltip title="삭제하기">
                            <IconButton
                                onClick={onDelete}
                                size="small"
                                sx={{
                                    backgroundColor: '#FFEBEE',
                                    color: '#D32F2F',
                                    border: '1px solid #FFCDD2',
                                    '&:hover': {
                                        backgroundColor: '#FFCDD2',
                                        transform: 'scale(1.05)'
                                    },
                                    transition: 'all 0.2s ease-in-out'
                                }}
                            >
                                <CloseIcon fontSize="small" />
                            </IconButton>
                        </Tooltip>
                    </>
                )}

                {/* 공용 템플릿의 Admin 삭제 버튼 */}
                {isPublicTemplate && userRole === 'ADMIN' && onDelete && (
                    <Tooltip title="삭제">
                        <IconButton
                            onClick={onDelete}
                            size="small"
                            sx={{
                                backgroundColor: '#FFEBEE',
                                color: '#D32F2F',
                                border: '1px solid #FFCDD2',
                                '&:hover': {
                                    backgroundColor: '#FFCDD2',
                                    transform: 'scale(1.05)'
                                },
                                transition: 'all 0.2s ease-in-out'
                            }}
                        >
                            <CloseIcon fontSize="small" />
                        </IconButton>
                    </Tooltip>
                )}

                {/* 사용하기 버튼 - 현대적인 스타일 */}
                {onUse && (
                    <Button
                        variant="contained"
                        fullWidth
                        onClick={onUse}
                        sx={{
                            background: 'linear-gradient(135deg, #1890ff 0%, #40a9ff 100%)',
                            color: 'white',
                            fontWeight: 600,
                            fontSize: '14px',
                            textTransform: 'none',
                            borderRadius: '12px',
                            border: '1px solid rgba(255, 255, 255, 0.2)',
                            ml: 1,
                            py: 1.2,
                            boxShadow: '0 4px 16px rgba(24, 144, 255, 0.3)',
                            fontFamily: '"Spoqa Han Sans Neo", sans-serif',
                            '&:hover': {
                                background: 'linear-gradient(135deg, #40a9ff 0%, #1890ff 100%)',
                                transform: 'translateY(-2px)',
                                boxShadow: '0 6px 20px rgba(24, 144, 255, 0.4)'
                            },
                            transition: 'all 0.3s ease'
                        }}
                    >
                        ✨ 템플릿 사용하기
                    </Button>
                )}
            </Box>
        </Box>
    );
}
