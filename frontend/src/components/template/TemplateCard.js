// src/components/template/TemplateCard.jsx
import { Card, CardContent, CardActions, IconButton, Typography, Tooltip, Button, Box, Chip } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import ShareIcon from '@mui/icons-material/Share';
import EditIcon from '@mui/icons-material/Edit';

export default function TemplateCard({ template, onDelete, onShare, onEdit, onUse, showActions, isPublicTemplate }) {
    
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
        <Card 
            className="shadow-md rounded-2xl" 
            sx={{ 
                height: '200px', 
                display: 'flex', 
                flexDirection: 'column',
                width: '100%'
            }}
        >
            <CardContent sx={{ flexGrow: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                    <Typography 
                        variant="subtitle1" 
                        sx={{ 
                            fontWeight: 600,
                            display: '-webkit-box',
                            WebkitLineClamp: 2,
                            WebkitBoxOrient: 'vertical',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            flex: 1,
                            mr: 1
                        }}
                    >
                        {title}
                    </Typography>
                    {!isPublicTemplate && getStatusChip(template.status)}
                </Box>
                <Typography 
                    variant="body2" 
                    sx={{ 
                        color: 'text.secondary',
                        display: '-webkit-box',
                        WebkitLineClamp: 3,
                        WebkitBoxOrient: 'vertical',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        lineHeight: 1.4,
                        mb: 2
                    }}
                >
                    {content}
                </Typography>
                
                {/* 자세히보기 버튼 - 내용 아래에 배치 */}
                {isPublicTemplate && buttonTitle && (
                    <Box sx={{ mt: 'auto', display: 'flex', justifyContent: 'center' }}>
                        <Button 
                            variant="outlined" 
                            size="small"
                            disabled
                            sx={{ 
                                color: 'text.secondary', 
                                borderColor: 'text.secondary',
                                textTransform: 'none'
                            }}
                        >
                            {buttonTitle}
                        </Button>
                    </Box>
                )}
            </CardContent>

            {/* 개인 템플릿의 액션 버튼들 (공유/삭제) */}
            {!isPublicTemplate && showActions && (
                <CardActions sx={{ justifyContent: 'flex-end', pt: 0, pb: 1 }}>
                    <Tooltip title="공유하기">
                        <IconButton 
                            onClick={onShare}
                            sx={{ 
                                bgcolor: '#f5f5f5',
                                '&:hover': { bgcolor: '#e0e0e0' },
                                mr: 1
                            }}
                        >
                            <ShareIcon fontSize="small" />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title="수정하기">
                        <IconButton
                            onClick={onEdit}
                            sx={{
                                bgcolor: '#f5f5f5',
                                '&:hover': { bgcolor: '#e0e0e0' },
                                mr: 1
                            }}
                        >
                            <EditIcon fontSize="small" />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title="삭제하기">
                        <IconButton 
                            onClick={onDelete}
                            sx={{ 
                                bgcolor: '#ffebee',
                                color: '#d32f2f',
                                '&:hover': { bgcolor: '#ffcdd2' }
                            }}
                        >
                            <CloseIcon fontSize="small" />
                        </IconButton>
                    </Tooltip>
                </CardActions>
            )}

            {/* 공용 템플릿의 Admin 삭제 버튼 */}
            {isPublicTemplate && userRole === 'ADMIN' && onDelete && (
                <CardActions sx={{ justifyContent: 'flex-end', pt: 0 }}>
                    <Tooltip title="삭제">
                        <IconButton onClick={onDelete}><CloseIcon /></IconButton>
                    </Tooltip>
                </CardActions>
            )}

            {/* 사용하기 버튼 - footer에 꽉 차게 */}
            {onUse && (
                <Box sx={{ p: 1 }}>
                    <Button 
                        variant="contained" 
                        fullWidth
                        onClick={onUse}
                        sx={{ 
                            bgcolor: '#343a40', 
                            color: 'white', 
                            '&:hover': { bgcolor: '#495057' },
                            borderRadius: 1
                        }}
                    >
                        사용
                    </Button>
                </Box>
            )}
        </Card>
    );
}
